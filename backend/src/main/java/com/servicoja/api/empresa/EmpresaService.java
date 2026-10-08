package com.servicoja.api.empresa;

import com.servicoja.api.assinatura.AssinaturaService;
import com.servicoja.dominio.avaliacao.AvaliacaoRepository;
import com.servicoja.dominio.categoria.Categoria;
import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.evento.EventoEmpresaRepository;
import com.servicoja.dominio.favorito.FavoritoRepository;
import com.servicoja.dominio.foto.Foto;
import com.servicoja.dominio.foto.FotoRepository;
import com.servicoja.dominio.horario.HorarioFuncionamento;
import com.servicoja.dominio.horario.HorarioFuncionamentoRepository;
import com.servicoja.dominio.notificacao.TipoNotificacao;
import com.servicoja.dominio.portfolio.Portfolio;
import com.servicoja.dominio.portfolio.PortfolioRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.AposCommit;
import com.servicoja.infra.Geolocalizacao;
import com.servicoja.infra.PageResposta;
import com.servicoja.infra.armazenamento.ArmazenamentoArquivos;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import com.servicoja.infra.servico.NotificacaoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EmpresaService {

    private static final String PREFIXO_ARQUIVO_LOCAL = "/uploads/";

    private final EmpresaRepository empresaRepository;
    private final CategoriaRepository categoriaRepository;
    private final FotoRepository fotoRepository;
    private final PortfolioRepository portfolioRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final FavoritoRepository favoritoRepository;
    private final HorarioFuncionamentoRepository horarioRepository;
    private final EventoEmpresaRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AssinaturaService assinaturaService;
    private final NotificacaoService notificacaoService;
    private final ArmazenamentoArquivos armazenamento;
    private final Clock relogio;
    private final String baseUrl;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            CategoriaRepository categoriaRepository,
            FotoRepository fotoRepository,
            PortfolioRepository portfolioRepository,
            AvaliacaoRepository avaliacaoRepository,
            FavoritoRepository favoritoRepository,
            HorarioFuncionamentoRepository horarioRepository,
            EventoEmpresaRepository eventoRepository,
            UsuarioRepository usuarioRepository,
            AssinaturaService assinaturaService,
            NotificacaoService notificacaoService,
            ArmazenamentoArquivos armazenamento,
            Clock relogio,
            @Value("${servico-ja.app.base-url}") String baseUrl) {
        this.empresaRepository = empresaRepository;
        this.categoriaRepository = categoriaRepository;
        this.fotoRepository = fotoRepository;
        this.portfolioRepository = portfolioRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.favoritoRepository = favoritoRepository;
        this.horarioRepository = horarioRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.assinaturaService = assinaturaService;
        this.notificacaoService = notificacaoService;
        this.armazenamento = armazenamento;
        this.relogio = relogio;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Transactional(readOnly = true)
    public PageResposta<EmpresaDtos.EmpresaSimplesResposta> buscarPublico(
            EmpresaDtos.FiltroBusca filtro, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), 50));
        LocalDateTime agora = LocalDateTime.now(relogio);
        boolean porDistancia = filtro.temLocalizacao();
        Page<Empresa> resultado = empresaRepository.buscar(
                filtro.categoriaId(), normalizar(filtro.nome()), normalizar(filtro.cidade()), normalizar(filtro.uf()),
                filtro.somenteAbertas(), agora.getDayOfWeek().getValue(), agora.toLocalTime(),
                porDistancia,
                porDistancia ? BigDecimal.valueOf(filtro.latitude()) : BigDecimal.ZERO,
                porDistancia ? BigDecimal.valueOf(filtro.longitude()) : BigDecimal.ZERO,
                porDistancia ? BigDecimal.valueOf(Geolocalizacao.fatorLongitude(filtro.latitude())) : BigDecimal.ONE,
                pageable);

        Map<Long, List<HorarioFuncionamento>> horariosPorEmpresa = horarioRepository
                .findByEmpresaIdIn(resultado.getContent().stream().map(Empresa::getId).toList()).stream()
                .collect(Collectors.groupingBy(h -> h.getEmpresa().getId()));
        return PageResposta.de(resultado.map(empresa -> converterSimples(
                empresa, horariosPorEmpresa.getOrDefault(empresa.getId(), List.of()), agora, filtro)));
    }

    @Transactional(readOnly = true)
    public EmpresaDtos.EmpresaResposta detalhar(Long id) {
        Empresa empresa = obter(id);
        if (!Boolean.TRUE.equals(empresa.getAprovada())) {
            throw new RecursoNaoEncontradoException("Empresa nao encontrada.");
        }
        return converterCompleto(empresa);
    }

    @Transactional(readOnly = true)
    public List<EmpresaDtos.EmpresaResposta> listarMinhas(Usuario usuario) {
        return empresaRepository.findByUsuarioIdAndExcluidaEmIsNull(usuario.getId()).stream()
                .map(this::converterCompleto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResposta<EmpresaDtos.EmpresaResposta> buscarAdministrativo(
            Long categoriaId, String nome, String cidade, String uf, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), 50));
        Page<Empresa> resultado = empresaRepository.buscarAdministrativo(
                categoriaId, normalizar(nome), normalizar(cidade), normalizar(uf), pageable);
        return PageResposta.de(resultado.map(this::converterCompleto));
    }

    @Transactional
    public EmpresaDtos.EmpresaResposta criar(Usuario usuario, EmpresaDtos.EmpresaRequest requisicao) {
        if (usuario.getPerfil() != Perfil.EMPRESA) {
            throw new NegocioException("Apenas usuarios com perfil de empresa podem cadastrar empresas.");
        }
        Categoria categoria = categoriaRepository.findById(requisicao.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria nao encontrada."));

        Empresa empresa = new Empresa();
        empresa.setUsuario(usuario);
        empresa.setCategoria(categoria);
        aplicarDados(empresa, requisicao);
        empresa.setAprovada(false);
        empresa.setDestaque(false);
        empresaRepository.save(empresa);
        substituirHorarios(empresa, requisicao.horarios());

        notificarAdmins("Nova empresa para aprovacao",
                "A empresa \"" + empresa.getNome() + "\" aguarda aprovacao.");
        return converterCompleto(empresa);
    }

    /**
     * Alterar o que aparece como conteudo da empresa (nome, categoria, descricoes, contatos, site,
     * redes) exige nova aprovacao; endereco, localizacao e horarios podem mudar sem moderacao.
     */
    @Transactional
    public EmpresaDtos.EmpresaResposta atualizar(Long id, Usuario usuario, EmpresaDtos.EmpresaRequest requisicao) {
        Empresa empresa = obter(id);
        verificarProprietario(empresa, usuario);
        ConteudoModerado conteudoAnterior = ConteudoModerado.de(empresa);
        if (requisicao.categoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(requisicao.categoriaId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria nao encontrada."));
            empresa.setCategoria(categoria);
        }
        aplicarDados(empresa, requisicao);
        substituirHorarios(empresa, requisicao.horarios());

        boolean estavaAprovada = Boolean.TRUE.equals(empresa.getAprovada());
        if (estavaAprovada && !conteudoAnterior.equals(ConteudoModerado.de(empresa))) {
            empresa.setAprovada(false);
            empresa.setDestaque(false);
            notificarAdmins("Empresa editada, nova aprovacao necessaria",
                    "A empresa \"" + empresa.getNome() + "\" foi editada e aguarda nova aprovacao.");
        }
        return converterCompleto(empresaRepository.save(empresa));
    }

    @Transactional
    public void excluir(Long id, Usuario usuario) {
        Empresa empresa = obter(id);
        verificarProprietario(empresa, usuario);
        removerDaPlataforma(empresa);
    }

    /**
     * Tira a empresa da plataforma: cancela a assinatura Premium no gateway, apaga avaliacoes,
     * favoritos, fotos, portfolio e arquivos enviados, e anonimiza o registro, que fica apenas
     * como historico financeiro. Usado na exclusao da empresa e na exclusao da conta do dono.
     */
    @Transactional
    public void removerDaPlataforma(Empresa empresa) {
        Long empresaId = empresa.getId();
        assinaturaService.cancelarAssinaturasDaEmpresa(empresa);
        avaliacaoRepository.deleteByEmpresaId(empresaId);
        favoritoRepository.deleteByEmpresaId(empresaId);
        fotoRepository.deleteByEmpresaId(empresaId);
        portfolioRepository.deleteByEmpresaId(empresaId);
        horarioRepository.deleteByEmpresaId(empresaId);
        eventoRepository.deleteByEmpresaId(empresaId);
        empresa.marcarComoExcluida();
        empresaRepository.save(empresa);

        String pasta = pastaDaEmpresa(empresa);
        AposCommit.executar(() -> armazenamento.removerPasta(pasta));
    }

    @Transactional
    public EmpresaDtos.FotoResposta adicionarFoto(
            Long id, Usuario usuario, MultipartFile arquivo, String descricao, Integer ordem) {
        Empresa empresa = obter(id);
        verificarProprietario(empresa, usuario);
        exigirPremium(empresa, "O envio de fotos e exclusivo para empresas Premium.");
        String caminho = armazenamento.salvar(arquivo, pastaDaEmpresa(empresa) + "/fotos");
        Foto foto = new Foto();
        foto.setEmpresa(empresa);
        foto.setUrl(caminho);
        foto.setDescricao(descricao);
        foto.setOrdem(ordem != null ? ordem : 0);
        return converterFoto(fotoRepository.save(foto));
    }

    @Transactional
    public void removerFoto(Long empresaId, Long fotoId, Usuario usuario) {
        Empresa empresa = obter(empresaId);
        verificarProprietario(empresa, usuario);
        Foto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Foto nao encontrada."));
        if (!foto.getEmpresa().getId().equals(empresaId)) {
            throw new NegocioException("A foto pertence a outra empresa.");
        }
        fotoRepository.delete(foto);
        removerArquivoAposCommit(empresa, foto.getUrl());
    }

    @Transactional
    public EmpresaDtos.EmpresaResposta atualizarLogo(Long id, Usuario usuario, MultipartFile arquivo) {
        Empresa empresa = obter(id);
        verificarProprietario(empresa, usuario);
        String logoAntigo = empresa.getLogoUrl();
        empresa.setLogoUrl(armazenamento.salvar(arquivo, pastaDaEmpresa(empresa) + "/logo"));
        empresaRepository.save(empresa);
        removerArquivoAposCommit(empresa, logoAntigo);
        return converterCompleto(empresa);
    }

    @Transactional
    public EmpresaDtos.PortfolioResposta adicionarPortfolio(Long id, Usuario usuario, EmpresaDtos.PortfolioRequest requisicao) {
        Empresa empresa = obter(id);
        verificarProprietario(empresa, usuario);
        exigirPremium(empresa, "O portfolio e exclusivo para empresas Premium.");
        Portfolio portfolio = new Portfolio();
        portfolio.setEmpresa(empresa);
        portfolio.setTitulo(requisicao.titulo());
        portfolio.setDescricao(requisicao.descricao());
        portfolio.setUrlMidia(requisicao.urlMidia());
        return converterPortfolio(portfolioRepository.save(portfolio));
    }

    @Transactional
    public void removerPortfolio(Long empresaId, Long portfolioId, Usuario usuario) {
        Empresa empresa = obter(empresaId);
        verificarProprietario(empresa, usuario);
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de portfolio nao encontrado."));
        if (!portfolio.getEmpresa().getId().equals(empresaId)) {
            throw new NegocioException("O item de portfolio pertence a outra empresa.");
        }
        portfolioRepository.delete(portfolio);
    }

    @Transactional
    public EmpresaDtos.MensagemResposta ativarDestaque(Long id, Usuario usuario) {
        Empresa empresa = obter(id);
        verificarProprietario(empresa, usuario);
        exigirPremium(empresa, "O destaque e exclusivo para empresas com Premium ativo.");
        if (!Boolean.TRUE.equals(empresa.getAprovada())) {
            throw new NegocioException("A empresa precisa ser aprovada para receber destaque.");
        }
        empresa.setDestaque(true);
        empresaRepository.save(empresa);
        return new EmpresaDtos.MensagemResposta("Destaque ativado.");
    }

    @Transactional
    public EmpresaDtos.MensagemResposta removerDestaque(Long id, Usuario usuario) {
        Empresa empresa = obter(id);
        verificarProprietario(empresa, usuario);
        empresa.setDestaque(false);
        empresaRepository.save(empresa);
        return new EmpresaDtos.MensagemResposta("Destaque removido.");
    }

    @Transactional
    public EmpresaDtos.EmpresaResposta aprovar(Long id, boolean aprovada) {
        Empresa empresa = obter(id);
        empresa.setAprovada(aprovada);
        empresaRepository.save(empresa);
        notificacaoService.enviar(empresa.getUsuario(),
                aprovada ? "Empresa aprovada" : "Empresa reprovada",
                "Sua empresa \"" + empresa.getNome() + "\" foi "
                        + (aprovada ? "aprovada e ja aparece nas buscas." : "reprovada pela moderacao."),
                TipoNotificacao.MODERACAO);
        return converterCompleto(empresa);
    }

    private Empresa obter(Long id) {
        return empresaRepository.findByIdAndExcluidaEmIsNull(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada."));
    }

    private String pastaDaEmpresa(Empresa empresa) {
        return "empresas/" + empresa.getId();
    }

    /**
     * Apaga um arquivo enviado so depois do commit e apenas se ele estiver na pasta da propria
     * empresa: um caminho antigo nunca pode levar a remocao de arquivos de outra empresa.
     */
    private void removerArquivoAposCommit(Empresa empresa, String caminho) {
        String pasta = pastaDaEmpresa(empresa);
        AposCommit.executar(() -> armazenamento.remover(caminho, pasta));
    }

    private void verificarProprietario(Empresa empresa, Usuario usuario) {
        if (!empresa.podeSerGerenciadaPor(usuario)) {
            throw new NegocioException("Voce nao tem permissao para alterar esta empresa.");
        }
    }

    /** {@code null} mantem os horarios atuais; uma lista (mesmo vazia) substitui todos. */
    private void substituirHorarios(Empresa empresa, List<EmpresaDtos.HorarioDto> horarios) {
        if (horarios == null) {
            return;
        }
        validarHorarios(horarios);
        horarioRepository.deleteByEmpresaId(empresa.getId());
        horarioRepository.saveAll(horarios.stream()
                .map(h -> new HorarioFuncionamento(empresa, h.diaSemana(), h.abre(), h.fecha()))
                .toList());
    }

    private void validarHorarios(List<EmpresaDtos.HorarioDto> horarios) {
        Map<Integer, List<EmpresaDtos.HorarioDto>> porDia = horarios.stream()
                .collect(Collectors.groupingBy(EmpresaDtos.HorarioDto::diaSemana));
        for (List<EmpresaDtos.HorarioDto> doDia : porDia.values()) {
            List<EmpresaDtos.HorarioDto> ordenados = doDia.stream()
                    .sorted(Comparator.comparing(EmpresaDtos.HorarioDto::abre))
                    .toList();
            for (int i = 0; i < ordenados.size(); i++) {
                EmpresaDtos.HorarioDto atual = ordenados.get(i);
                if (!atual.fecha().isAfter(atual.abre())) {
                    throw new NegocioException("O horario de fechamento deve ser depois do de abertura.");
                }
                if (i > 0 && atual.abre().isBefore(ordenados.get(i - 1).fecha())) {
                    throw new NegocioException("Os horarios de um mesmo dia nao podem se sobrepor.");
                }
            }
        }
    }

    private List<HorarioFuncionamento> horariosDe(Empresa empresa) {
        return horarioRepository.findByEmpresaIdOrderByDiaSemanaAscAbreAsc(empresa.getId());
    }

    private void exigirPremium(Empresa empresa, String mensagem) {
        if (!empresa.isPerfilCompleto()) {
            throw new NegocioException(mensagem);
        }
    }

    private void aplicarDados(Empresa empresa, EmpresaDtos.EmpresaRequest r) {
        empresa.setNome(r.nome().trim());
        empresa.setDescricaoCurta(r.descricaoCurta());
        empresa.setDescricaoCompleta(r.descricaoCompleta());
        empresa.setTelefone(r.telefone());
        empresa.setWhatsapp(r.whatsapp());
        empresa.setEmailContato(r.emailContato());
        empresa.setCep(r.cep());
        empresa.setEndereco(r.endereco());
        empresa.setNumero(r.numero());
        empresa.setBairro(r.bairro());
        empresa.setCidade(r.cidade().trim());
        empresa.setUf(r.uf().trim().toUpperCase());
        empresa.setLatitude(r.latitude());
        empresa.setLongitude(r.longitude());
        empresa.setRedesSociais(r.redesSociais());
        empresa.setSite(r.site());
    }

    private void notificarAdmins(String titulo, String mensagem) {
        usuarioRepository.findByPerfil(Perfil.ADMIN)
                .forEach(admin -> notificacaoService.enviar(admin, titulo, mensagem, TipoNotificacao.MODERACAO));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private EmpresaDtos.EmpresaSimplesResposta converterSimples(
            Empresa empresa, List<HorarioFuncionamento> horarios, LocalDateTime agora, EmpresaDtos.FiltroBusca filtro) {
        Double distanciaKm = null;
        if (filtro.temLocalizacao() && empresa.temLocalizacao()) {
            double distancia = Geolocalizacao.distanciaKm(filtro.latitude(), filtro.longitude(),
                    empresa.getLatitude().doubleValue(), empresa.getLongitude().doubleValue());
            distanciaKm = BigDecimal.valueOf(distancia).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
        return new EmpresaDtos.EmpresaSimplesResposta(
                empresa.getId(),
                empresa.getNome(),
                categoriaDe(empresa),
                urlAbsoluta(empresa.getLogoUrl()),
                empresa.getCidade(),
                empresa.getUf(),
                empresa.getLatitude(),
                empresa.getLongitude(),
                distanciaKm,
                HorarioFuncionamento.algumCobre(horarios, agora),
                !horarios.isEmpty(),
                Boolean.TRUE.equals(empresa.getPremiumAtivo()),
                Boolean.TRUE.equals(empresa.getDestaque()),
                empresa.isPerfilCompleto(),
                empresa.getMediaAvaliacoes(),
                empresa.getTotalAvaliacoes());
    }

    private EmpresaDtos.EmpresaResposta converterCompleto(Empresa empresa) {
        boolean perfilCompleto = empresa.isPerfilCompleto();
        List<HorarioFuncionamento> horarios = horariosDe(empresa);

        List<EmpresaDtos.FotoResposta> fotos = new ArrayList<>();
        List<EmpresaDtos.PortfolioResposta> portfolios = new ArrayList<>();
        if (perfilCompleto) {
            fotos = fotoRepository.findByEmpresaIdOrderByOrdemAsc(empresa.getId()).stream()
                    .map(this::converterFoto)
                    .toList();
            portfolios = portfolioRepository.findByEmpresaIdOrderByCriadoEmDesc(empresa.getId()).stream()
                    .map(this::converterPortfolio)
                    .toList();
        }

        return new EmpresaDtos.EmpresaResposta(
                empresa.getId(),
                empresa.getNome(),
                categoriaDe(empresa),
                empresa.getUsuario().getNome(),
                empresa.getDescricaoCurta(),
                perfilCompleto ? empresa.getDescricaoCompleta() : null,
                urlAbsoluta(empresa.getLogoUrl()),
                empresa.getTelefone(),
                empresa.getWhatsapp(),
                empresa.getEmailContato(),
                empresa.getCep(),
                empresa.getEndereco(),
                empresa.getNumero(),
                empresa.getBairro(),
                empresa.getCidade(),
                empresa.getUf(),
                empresa.getLatitude(),
                empresa.getLongitude(),
                horarios.stream()
                        .map(h -> new EmpresaDtos.HorarioDto(h.getDiaSemana(), h.getAbre(), h.getFecha()))
                        .toList(),
                HorarioFuncionamento.algumCobre(horarios, LocalDateTime.now(relogio)),
                perfilCompleto ? empresa.getRedesSociais() : null,
                perfilCompleto ? empresa.getSite() : null,
                Boolean.TRUE.equals(empresa.getPremiumAtivo()),
                empresa.getPremiumAte(),
                Boolean.TRUE.equals(empresa.getDestaque()),
                Boolean.TRUE.equals(empresa.getAprovada()),
                perfilCompleto,
                empresa.getMediaAvaliacoes(),
                empresa.getTotalAvaliacoes(),
                fotos,
                portfolios);
    }

    private EmpresaDtos.CategoriaSimplificada categoriaDe(Empresa empresa) {
        Categoria categoria = empresa.getCategoria();
        return new EmpresaDtos.CategoriaSimplificada(categoria.getId(), categoria.getNome(), categoria.getIcone());
    }

    private EmpresaDtos.FotoResposta converterFoto(Foto foto) {
        return new EmpresaDtos.FotoResposta(foto.getId(), urlAbsoluta(foto.getUrl()), foto.getDescricao(), foto.getOrdem());
    }

    private String urlAbsoluta(String caminho) {
        if (caminho != null && caminho.startsWith(PREFIXO_ARQUIVO_LOCAL)) {
            return baseUrl + caminho;
        }
        return caminho;
    }

    private EmpresaDtos.PortfolioResposta converterPortfolio(Portfolio portfolio) {
        return new EmpresaDtos.PortfolioResposta(
                portfolio.getId(), portfolio.getTitulo(), portfolio.getDescricao(), portfolio.getUrlMidia());
    }

    /** Campos exibidos publicamente que passam pela moderacao do administrador. */
    private record ConteudoModerado(
            String nome,
            Long categoriaId,
            String descricaoCurta,
            String descricaoCompleta,
            String telefone,
            String whatsapp,
            String emailContato,
            String redesSociais,
            String site) {

        static ConteudoModerado de(Empresa empresa) {
            return new ConteudoModerado(
                    empresa.getNome(),
                    empresa.getCategoria().getId(),
                    textoOuNulo(empresa.getDescricaoCurta()),
                    textoOuNulo(empresa.getDescricaoCompleta()),
                    textoOuNulo(empresa.getTelefone()),
                    textoOuNulo(empresa.getWhatsapp()),
                    textoOuNulo(empresa.getEmailContato()),
                    textoOuNulo(empresa.getRedesSociais()),
                    textoOuNulo(empresa.getSite()));
        }

        /** O app envia "" para campos vazios; vazio e nulo contam como o mesmo conteudo. */
        private static String textoOuNulo(String valor) {
            return Objects.requireNonNullElse(valor, "").isBlank() ? null : valor.trim();
        }
    }
}
