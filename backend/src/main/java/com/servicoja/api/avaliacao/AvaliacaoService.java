package com.servicoja.api.avaliacao;

import com.servicoja.dominio.avaliacao.Avaliacao;
import com.servicoja.dominio.avaliacao.AvaliacaoRepository;
import com.servicoja.dominio.avaliacao.StatusAvaliacao;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.notificacao.TipoNotificacao;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.infra.PageResposta;
import com.servicoja.infra.excecao.LimiteExcedidoException;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import com.servicoja.infra.seguranca.LimitadorRequisicoes;
import com.servicoja.infra.servico.NotificacaoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final EmpresaRepository empresaRepository;
    private final NotificacaoService notificacaoService;
    private final LimitadorRequisicoes limitador;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            EmpresaRepository empresaRepository,
            NotificacaoService notificacaoService,
            LimitadorRequisicoes limitador) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.empresaRepository = empresaRepository;
        this.notificacaoService = notificacaoService;
        this.limitador = limitador;
    }

    @Transactional
    public AvaliacaoDtos.AvaliacaoResposta avaliar(Usuario usuario, Long empresaId, AvaliacaoDtos.AvaliacaoRequest requisicao) {
        if (usuario.getPerfil() != Perfil.CLIENTE) {
            throw new NegocioException("Apenas clientes podem avaliar empresas.");
        }
        if (!limitador.permitido("avaliacao:" + usuario.getId(), 5, Duration.ofHours(1))) {
            throw new LimiteExcedidoException("Limite de avaliacoes atingido. Tente novamente mais tarde.");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada."));
        if (!Boolean.TRUE.equals(empresa.getAprovada())) {
            throw new NegocioException("A empresa precisa ser aprovada para receber avaliacoes.");
        }

        if (empresa.getUsuario().getId().equals(usuario.getId())) {
            throw new NegocioException("Voce nao pode avaliar a propria empresa.");
        }
        if (avaliacaoRepository.existsByUsuarioIdAndEmpresaId(usuario.getId(), empresaId)) {
            throw new NegocioException("Voce ja avaliou esta empresa. Cada usuario pode avaliar uma vez.");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuario);
        avaliacao.setEmpresa(empresa);
        avaliacao.setNota(requisicao.nota());
        avaliacao.setComentario(requisicao.comentario());
        avaliacao.setStatus(StatusAvaliacao.PENDENTE);
        Avaliacao salva = avaliacaoRepository.save(avaliacao);

        notificacaoService.enviar(empresa.getUsuario(),
                "Nova avaliacao recebida",
                "Sua empresa \"" + empresa.getNome() + "\" recebeu uma nova avaliacao de "
                        + usuario.getNome() + " (nota " + requisicao.nota() + ").",
                TipoNotificacao.AVALIACAO);

        return converter(salva);
    }

    @Transactional(readOnly = true)
    public PageResposta<AvaliacaoDtos.AvaliacaoResposta> listarPorEmpresa(Long empresaId, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), 50),
                Sort.by(Sort.Direction.DESC, "criadoEm"));
        return PageResposta.de(avaliacaoRepository
                .findByEmpresaIdAndStatus(empresaId, StatusAvaliacao.APROVADA, pageable)
                .map(this::converter));
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoDtos.AvaliacaoResposta> listarMinhas(Usuario usuario) {
        return avaliacaoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuario.getId()).stream()
                .map(this::converter)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResposta<AvaliacaoDtos.AvaliacaoResposta> listarPendentes(int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), 50),
                Sort.by(Sort.Direction.ASC, "criadoEm"));
        return PageResposta.de(avaliacaoRepository
                .findByStatus(StatusAvaliacao.PENDENTE, pageable)
                .map(this::converter));
    }

    @Transactional
    public AvaliacaoDtos.AvaliacaoResposta moderar(Long id, AvaliacaoDtos.ModeraAvaliacaoRequest requisicao) {
        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliacao nao encontrada."));

        StatusAvaliacao novoStatus = requisicao.status();
        if (novoStatus == StatusAvaliacao.PENDENTE) {
            throw new NegocioException("A moderacao deve aprovar ou rejeitar a avaliacao.");
        }
        avaliacao.setStatus(novoStatus);
        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        recalcularMedia(salva.getEmpresa());

        notificacaoService.enviar(salva.getUsuario(),
                novoStatus == StatusAvaliacao.APROVADA ? "Avaliacao aprovada" : "Avaliacao rejeitada",
                "Sua avaliacao na empresa \"" + salva.getEmpresa().getNome() + "\" foi "
                        + (novoStatus == StatusAvaliacao.APROVADA ? "aprovada." : "rejeitada pela moderacao."),
                TipoNotificacao.MODERACAO);

        return converter(salva);
    }

    private void recalcularMedia(Empresa empresa) {
        double media = avaliacaoRepository.mediaNotaPorEmpresa(empresa.getId());
        long total = avaliacaoRepository.countByEmpresaIdAndStatus(empresa.getId(), StatusAvaliacao.APROVADA);
        empresa.setMediaAvaliacoes(BigDecimal.valueOf(media).setScale(1, RoundingMode.HALF_UP));
        empresa.setTotalAvaliacoes((int) total);
        empresaRepository.save(empresa);
    }

    private AvaliacaoDtos.AvaliacaoResposta converter(Avaliacao avaliacao) {
        return new AvaliacaoDtos.AvaliacaoResposta(
                avaliacao.getId(),
                avaliacao.getUsuario().getId(),
                avaliacao.getUsuario().getNome(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getStatus(),
                avaliacao.getCriadoEm());
    }
}
