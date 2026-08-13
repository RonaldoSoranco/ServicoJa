package com.servicoja.api.empresa;

import com.servicoja.dominio.categoria.Categoria;
import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.foto.Foto;
import com.servicoja.dominio.foto.FotoRepository;
import com.servicoja.dominio.notificacao.TipoNotificacao;
import com.servicoja.dominio.portfolio.Portfolio;
import com.servicoja.dominio.portfolio.PortfolioRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.PageResposta;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import com.servicoja.infra.servico.NotificacaoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final CategoriaRepository categoriaRepository;
    private final FotoRepository fotoRepository;
    private final PortfolioRepository portfolioRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            CategoriaRepository categoriaRepository,
            FotoRepository fotoRepository,
            PortfolioRepository portfolioRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoService notificacaoService) {
        this.empresaRepository = empresaRepository;
        this.categoriaRepository = categoriaRepository;
        this.fotoRepository = fotoRepository;
        this.portfolioRepository = portfolioRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(readOnly = true)
    public PageResposta<EmpresaDtos.EmpresaSimplesResposta> buscarPublico(
            Long categoriaId, String nome, String cidade, String uf, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), 50));
        Page<Empresa> resultado = empresaRepository.buscar(
                categoriaId, normalizar(nome), normalizar(cidade), normalizar(uf), pageable);
        return PageResposta.de(resultado.map(this::converterSimples));
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
        return empresaRepository.findByUsuarioId(usuario.getId()).stream()
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

        notificarAdmins("Nova empresa para aprovacao",
                "A empresa \"" + empresa.getNome() + "\" aguarda aprovacao.");
        return converterCompleto(empresa);
    }

    @Transactional
    public EmpresaDtos.EmpresaResposta atualizar(Long id, Usuario usuario, EmpresaDtos.EmpresaRequest requisicao) {
        Empresa empresa = obter(id);
        verificarProprietario(empresa, usuario);
        if (requisicao.categoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(requisicao.categoriaId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria nao encontrada."));
            empresa.setCategoria(categoria);
        }
        boolean estavaAprovada = Boolean.TRUE.equals(empresa.getAprovada());
        aplicarDados(empresa, requisicao);
        if (estavaAprovada) {
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
        empresaRepository.delete(empresa);
    }

    @Transactional
    public EmpresaDtos.FotoResposta adicionarFoto(Long id, Usuario usuario, EmpresaDtos.FotoRequest requisicao) {
        Empresa empresa = obter(id);
        verificarProprietario(empresa, usuario);
        exigirPremium(empresa, "O envio de fotos e exclusivo para empresas Premium.");
        Foto foto = new Foto();
        foto.setEmpresa(empresa);
        foto.setUrl(requisicao.url());
        foto.setDescricao(requisicao.descricao());
        foto.setOrdem(requisicao.ordem() != null ? requisicao.ordem() : 0);
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
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada."));
    }

    private void verificarProprietario(Empresa empresa, Usuario usuario) {
        if (usuario.getPerfil() == Perfil.ADMIN) {
            return;
        }
        if (!empresa.getUsuario().getId().equals(usuario.getId())) {
            throw new NegocioException("Voce nao tem permissao para alterar esta empresa.");
        }
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
        empresa.setLogoUrl(r.logoUrl());
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
        empresa.setHorarioFuncionamento(r.horarioFuncionamento());
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

    private EmpresaDtos.EmpresaSimplesResposta converterSimples(Empresa empresa) {
        return new EmpresaDtos.EmpresaSimplesResposta(
                empresa.getId(),
                empresa.getNome(),
                new EmpresaDtos.CategoriaSimplificada(
                        empresa.getCategoria().getId(), empresa.getCategoria().getNome()),
                empresa.getLogoUrl(),
                empresa.getCidade(),
                empresa.getUf(),
                Boolean.TRUE.equals(empresa.getPremiumAtivo()),
                Boolean.TRUE.equals(empresa.getDestaque()),
                empresa.isPerfilCompleto(),
                empresa.getMediaAvaliacoes(),
                empresa.getTotalAvaliacoes());
    }

    private EmpresaDtos.EmpresaResposta converterCompleto(Empresa empresa) {
        boolean perfilCompleto = empresa.isPerfilCompleto();

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
                new EmpresaDtos.CategoriaSimplificada(
                        empresa.getCategoria().getId(), empresa.getCategoria().getNome()),
                empresa.getUsuario().getNome(),
                empresa.getDescricaoCurta(),
                perfilCompleto ? empresa.getDescricaoCompleta() : null,
                empresa.getLogoUrl(),
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
                perfilCompleto ? empresa.getHorarioFuncionamento() : null,
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

    private EmpresaDtos.FotoResposta converterFoto(Foto foto) {
        return new EmpresaDtos.FotoResposta(foto.getId(), foto.getUrl(), foto.getDescricao(), foto.getOrdem());
    }

    private EmpresaDtos.PortfolioResposta converterPortfolio(Portfolio portfolio) {
        return new EmpresaDtos.PortfolioResposta(
                portfolio.getId(), portfolio.getTitulo(), portfolio.getDescricao(), portfolio.getUrlMidia());
    }
}
