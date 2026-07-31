package com.servicoja.api.assinatura;

import tools.jackson.databind.ObjectMapper;
import com.servicoja.dominio.assinatura.Assinatura;
import com.servicoja.dominio.assinatura.AssinaturaRepository;
import com.servicoja.dominio.assinatura.StatusAssinatura;
import com.servicoja.dominio.assinatura.TipoAssinatura;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.notificacao.TipoNotificacao;
import com.servicoja.dominio.pagamento.Pagamento;
import com.servicoja.dominio.pagamento.PagamentoRepository;
import com.servicoja.dominio.pagamento.StatusPagamento;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import com.servicoja.infra.servico.NotificacaoService;
import com.servicoja.pagamento.asaas.AsaasCliente;
import com.servicoja.pagamento.asaas.AsaasDtos;
import com.servicoja.pagamento.asaas.AsaasPropriedades;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AssinaturaService {

    private static final DateTimeFormatter DATA_FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AssinaturaRepository assinaturaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsaasCliente asaasCliente;
    private final AsaasPropriedades propriedades;
    private final NotificacaoService notificacaoService;
    private final ObjectMapper objectMapper;

    public AssinaturaService(
            AssinaturaRepository assinaturaRepository,
            PagamentoRepository pagamentoRepository,
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            AsaasCliente asaasCliente,
            AsaasPropriedades propriedades,
            NotificacaoService notificacaoService,
            ObjectMapper objectMapper) {
        this.assinaturaRepository = assinaturaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.asaasCliente = asaasCliente;
        this.propriedades = propriedades;
        this.notificacaoService = notificacaoService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AssinaturaDtos.AssinaturaResposta criar(Usuario usuario, AssinaturaDtos.CriarAssinaturaRequest requisicao) {
        if (usuario.getPerfil() != Perfil.EMPRESA) {
            throw new NegocioException("Apenas empresas podem contratar a assinatura Premium.");
        }
        Empresa empresa = empresaRepository.findById(requisicao.empresaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada."));
        if (!empresa.getUsuario().getId().equals(usuario.getId())) {
            throw new NegocioException("Voce nao pode assinar a assinatura desta empresa.");
        }
        if (assinaturaRepository.existsByEmpresaIdAndStatus(empresa.getId(), StatusAssinatura.ATIVA)) {
            throw new NegocioException("Esta empresa ja possui uma assinatura Premium ativa.");
        }

        TipoAssinatura tipo = requisicao.tipo();
        BigDecimal valor = tipo == TipoAssinatura.ANUAL ? propriedades.getValorAnual() : propriedades.getValorMensal();
        String ciclo = tipo == TipoAssinatura.ANUAL ? "YEARLY" : "MONTHLY";
        String vencimento = LocalDate.now().plusDays(1).format(DATA_FORMATO);

        String clienteId = garantirClienteAsaas(usuario);

        AsaasDtos.AssinaturaResponse assinaturaAsaas = asaasCliente.criarAssinatura(
                new AsaasDtos.CriarAssinaturaRequest(
                        clienteId, "PIX", valor, vencimento, ciclo,
                        "Assinatura Premium - Servico Ja"));

        AsaasDtos.PagamentoResponse pagamentoAsaas = asaasCliente.criarPagamento(
                new AsaasDtos.CriarPagamentoRequest(
                        clienteId, "PIX", valor, vencimento, assinaturaAsaas.id(),
                        "Pagamento da assinatura Premium - Servico Ja"));

        OffsetDateTime agora = OffsetDateTime.now();
        OffsetDateTime fim = tipo == TipoAssinatura.ANUAL ? agora.plusYears(1) : agora.plusMonths(1);

        Assinatura assinatura = new Assinatura();
        assinatura.setUsuario(usuario);
        assinatura.setEmpresa(empresa);
        assinatura.setTipo(tipo);
        assinatura.setStatus(StatusAssinatura.ATIVA);
        assinatura.setInicioEm(agora);
        assinatura.setFimEm(fim);
        assinatura.setAsaasAssinaturaId(assinaturaAsaas.id());
        assinaturaRepository.save(assinatura);

        Pagamento pagamento = new Pagamento();
        pagamento.setAssinatura(assinatura);
        pagamento.setEmpresa(empresa);
        pagamento.setAsaasPagamentoId(pagamentoAsaas.id());
        pagamento.setValor(valor);
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setMetodoPagamento("PIX");
        pagamento.setLinkPagamento(pagamentoAsaas.paymentLink() != null
                ? pagamentoAsaas.paymentLink() : pagamentoAsaas.invoiceUrl());
        pagamentoRepository.save(pagamento);

        notificacaoService.enviar(usuario,
                "Assinatura solicitada",
                "Sua assinatura Premium para \"" + empresa.getNome() + "\" esta aguardando o pagamento.",
                TipoNotificacao.ASSINATURA);

        return converter(assinatura, pagamento, empresa);
    }

    @Transactional
    public AssinaturaDtos.MensagemResposta cancelar(Usuario usuario, Long assinaturaId) {
        Assinatura assinatura = assinaturaRepository.findById(assinaturaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Assinatura nao encontrada."));
        if (!assinatura.getUsuario().getId().equals(usuario.getId())) {
            throw new NegocioException("Voce nao pode cancelar esta assinatura.");
        }
        if (assinatura.getStatus() != StatusAssinatura.ATIVA) {
            throw new NegocioException("Esta assinatura nao esta ativa.");
        }

        desativarPremium(assinatura, StatusAssinatura.CANCELADA,
                "Sua assinatura Premium foi cancelada. O destaque da empresa foi removido imediatamente.");

        if (assinatura.getAsaasAssinaturaId() != null) {
            try {
                asaasCliente.cancelarAssinatura(assinatura.getAsaasAssinaturaId());
            } catch (Exception ignorada) {
                // O cancelamento local ja ocorreu; a falha no gateway sera tratada pelo webhook.
            }
        }
        return new AssinaturaDtos.MensagemResposta("Assinatura cancelada. O destaque da empresa foi removido imediatamente.");
    }

    @Transactional
    public AssinaturaDtos.AssinaturaResposta obterAssinaturaAtiva(Usuario usuario, Long empresaId) {
        return assinaturaRepository.findFirstByEmpresaIdAndStatusOrderByIdDesc(empresaId, StatusAssinatura.ATIVA)
                .map(assinatura -> converter(assinatura,
                        pagamentoRepository.findByAssinaturaId(assinatura.getId()).orElse(null),
                        assinatura.getEmpresa()))
                .orElse(null);
    }

    @Transactional
    public void processarWebhook(String corpo, String assinaturaAsaas) {
        AsaasDtos.WebhookPayload payload;
        try {
            payload = objectMapper.readValue(corpo, AsaasDtos.WebhookPayload.class);
        } catch (Exception ex) {
            throw new NegocioException("Payload de webhook invalido.");
        }

        String evento = payload.event();
        if (evento == null) {
            return;
        }

        switch (evento) {
            case "PAYMENT_RECEIVED", "PAYMENT_CONFIRMED" -> confirmarPagamento(payload);
            case "PAYMENT_OVERDUE" -> marcarPagamentoAtrasado(payload);
            case "PAYMENT_REFUNDED", "PAYMENT_DELETED" -> estornarPagamento(payload);
            case "SUBSCRIPTION_CANCELLED", "SUBSCRIPTION_DELETED" -> cancelarAssinaturaRemota(payload);
            case "SUBSCRIPTION_EXPIRED" -> expirarAssinatura(payload);
            default -> {
                // Eventos sem impacto no MVP.
            }
        }
    }

    private void confirmarPagamento(AsaasDtos.WebhookPayload payload) {
        if (payload.payment() == null || payload.payment().id() == null) {
            return;
        }
        pagamentoRepository.findByAsaasPagamentoId(payload.payment().id()).ifPresent(pagamento -> {
            pagamento.setStatus(StatusPagamento.PAGO);
            pagamento.setPagoEm(OffsetDateTime.now());
            pagamentoRepository.save(pagamento);

            Assinatura assinatura = pagamento.getAssinatura();
            if (assinatura == null) {
                return;
            }
            ativarPremium(assinatura);
        });
    }

    private void marcarPagamentoAtrasado(AsaasDtos.WebhookPayload payload) {
        if (payload.payment() == null || payload.payment().id() == null) {
            return;
        }
        pagamentoRepository.findByAsaasPagamentoId(payload.payment().id()).ifPresent(pagamento -> {
            pagamento.setStatus(StatusPagamento.PENDENTE);
            pagamentoRepository.save(pagamento);
        });
    }

    private void estornarPagamento(AsaasDtos.WebhookPayload payload) {
        if (payload.payment() == null || payload.payment().id() == null) {
            return;
        }
        pagamentoRepository.findByAsaasPagamentoId(payload.payment().id()).ifPresent(pagamento -> {
            pagamento.setStatus(StatusPagamento.ESTORNADO);
            pagamentoRepository.save(pagamento);
            if (pagamento.getAssinatura() != null) {
                desativarPremium(pagamento.getAssinatura(), StatusAssinatura.CANCELADA,
                        "O pagamento foi estornado e o Premium foi desativado.");
            }
        });
    }

    private void cancelarAssinaturaRemota(AsaasDtos.WebhookPayload payload) {
        if (payload.subscription() == null || payload.subscription().id() == null) {
            return;
        }
        assinaturaRepository.findByAsaasAssinaturaId(payload.subscription().id()).ifPresent(assinatura -> {
            if (assinatura.getStatus() == StatusAssinatura.ATIVA) {
                desativarPremium(assinatura, StatusAssinatura.CANCELADA,
                        "Sua assinatura foi cancelada no gateway. O destaque da empresa foi removido.");
            }
        });
    }

    private void expirarAssinatura(AsaasDtos.WebhookPayload payload) {
        if (payload.subscription() == null || payload.subscription().id() == null) {
            return;
        }
        assinaturaRepository.findByAsaasAssinaturaId(payload.subscription().id()).ifPresent(assinatura -> {
            if (assinatura.getStatus() == StatusAssinatura.ATIVA) {
                desativarPremium(assinatura, StatusAssinatura.EXPIRADA,
                        "Sua assinatura expirou. O destaque da empresa foi removido.");
            }
        });
    }

    private void ativarPremium(Assinatura assinatura) {
        Empresa empresa = assinatura.getEmpresa();
        OffsetDateTime agora = OffsetDateTime.now();
        OffsetDateTime fim = assinatura.getTipo() == TipoAssinatura.ANUAL ? agora.plusYears(1) : agora.plusMonths(1);
        assinatura.setFimEm(fim);
        empresa.setPremiumAtivo(true);
        empresa.setPremiumAte(fim);
        empresaRepository.save(empresa);
        notificacaoService.enviar(assinatura.getUsuario(),
                "Premium ativado",
                "Parabens! Sua empresa \"" + empresa.getNome() + "\" agora e Premium. O destaque esta disponivel.",
                TipoNotificacao.PAGAMENTO);
    }

    private void desativarPremium(Assinatura assinatura, StatusAssinatura status, String mensagem) {
        assinatura.setStatus(status);
        assinaturaRepository.save(assinatura);
        Empresa empresa = assinatura.getEmpresa();
        empresa.setPremiumAtivo(false);
        empresa.setPremiumAte(null);
        empresa.setDestaque(false);
        empresaRepository.save(empresa);
        notificacaoService.enviar(assinatura.getUsuario(), "Premium desativado", mensagem, TipoNotificacao.ASSINATURA);
    }

    private String garantirClienteAsaas(Usuario usuario) {
        if (usuario.getAsaasClienteId() != null) {
            return usuario.getAsaasClienteId();
        }
        AsaasDtos.ClienteResponse cliente = asaasCliente.criarCliente(
                new AsaasDtos.CriarClienteRequest(
                        usuario.getNome(),
                        usuario.getEmail(),
                        cpfFakeParaTeste()));
        usuario.setAsaasClienteId(cliente.id());
        usuarioRepository.save(usuario);
        return cliente.id();
    }

    private String cpfFakeParaTeste() {
        return "11144477735";
    }

    private AssinaturaDtos.AssinaturaResposta converter(Assinatura assinatura, Pagamento pagamento, Empresa empresa) {
        return new AssinaturaDtos.AssinaturaResposta(
                assinatura.getId(),
                empresa.getId(),
                assinatura.getTipo(),
                assinatura.getStatus(),
                assinatura.getInicioEm(),
                assinatura.getFimEm(),
                pagamento != null ? pagamento.getValor() : null,
                pagamento != null ? pagamento.getLinkPagamento() : null,
                Boolean.TRUE.equals(empresa.getPremiumAtivo()));
    }
}
