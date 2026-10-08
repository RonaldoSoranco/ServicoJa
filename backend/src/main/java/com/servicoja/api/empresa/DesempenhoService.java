package com.servicoja.api.empresa;

import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.evento.EventoEmpresa;
import com.servicoja.dominio.evento.EventoEmpresaRepository;
import com.servicoja.dominio.evento.TipoEventoEmpresa;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import com.servicoja.infra.seguranca.LimitadorRequisicoes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * Painel de desempenho: conta visualizacoes do perfil e cliques em WhatsApp, ligar e mapa.
 * Cada pessoa (IP) conta no maximo uma vez por tipo a cada 30 minutos, e o proprio dono nao conta.
 */
@Service
public class DesempenhoService {

    static final int DIAS_PERIODO = 30;
    private static final Duration INTERVALO_ENTRE_EVENTOS_IGUAIS = Duration.ofMinutes(30);

    private final EmpresaRepository empresaRepository;
    private final EventoEmpresaRepository eventoRepository;
    private final LimitadorRequisicoes limitador;
    private final Clock relogio;

    public DesempenhoService(
            EmpresaRepository empresaRepository,
            EventoEmpresaRepository eventoRepository,
            LimitadorRequisicoes limitador,
            Clock relogio) {
        this.empresaRepository = empresaRepository;
        this.eventoRepository = eventoRepository;
        this.limitador = limitador;
        this.relogio = relogio;
    }

    /**
     * @param usuarioId usuario autenticado, se houver; acessos do dono da empresa sao ignorados
     */
    @Transactional
    public void registrar(Long empresaId, TipoEventoEmpresa tipo, String ip, Long usuarioId) {
        Empresa empresa = empresaRepository.findByIdAndExcluidaEmIsNull(empresaId)
                .filter(e -> Boolean.TRUE.equals(e.getAprovada()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada."));
        if (usuarioId != null && empresa.getUsuario().getId().equals(usuarioId)) {
            return;
        }
        String chave = "evento:" + empresaId + ":" + tipo + ":" + ip;
        if (!limitador.permitido(chave, 1, INTERVALO_ENTRE_EVENTOS_IGUAIS)) {
            return;
        }
        eventoRepository.save(new EventoEmpresa(empresa, tipo, OffsetDateTime.now(relogio)));
    }

    @Transactional(readOnly = true)
    public EmpresaDtos.DesempenhoResposta consultar(Long empresaId, Usuario usuario) {
        Empresa empresa = empresaRepository.findByIdAndExcluidaEmIsNull(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada."));
        if (!empresa.podeSerGerenciadaPor(usuario)) {
            throw new NegocioException("Voce nao tem permissao para ver o desempenho desta empresa.");
        }

        OffsetDateTime agora = OffsetDateTime.now(relogio);
        OffsetDateTime inicioPeriodo = agora.minusDays(DIAS_PERIODO);
        Map<TipoEventoEmpresa, Long> atual = contar(empresaId, inicioPeriodo, agora);
        Map<TipoEventoEmpresa, Long> anterior = contar(empresaId, inicioPeriodo.minusDays(DIAS_PERIODO), inicioPeriodo);

        return new EmpresaDtos.DesempenhoResposta(
                DIAS_PERIODO,
                metrica(TipoEventoEmpresa.VISUALIZACAO, atual, anterior),
                metrica(TipoEventoEmpresa.CLIQUE_WHATSAPP, atual, anterior),
                metrica(TipoEventoEmpresa.CLIQUE_LIGAR, atual, anterior),
                metrica(TipoEventoEmpresa.CLIQUE_MAPA, atual, anterior));
    }

    private Map<TipoEventoEmpresa, Long> contar(Long empresaId, OffsetDateTime desde, OffsetDateTime ate) {
        Map<TipoEventoEmpresa, Long> totais = new EnumMap<>(TipoEventoEmpresa.class);
        eventoRepository.contarPorTipo(empresaId, desde, ate)
                .forEach(contagem -> totais.put(contagem.getTipo(), contagem.getTotal()));
        return totais;
    }

    private EmpresaDtos.MetricaResposta metrica(
            TipoEventoEmpresa tipo, Map<TipoEventoEmpresa, Long> atual, Map<TipoEventoEmpresa, Long> anterior) {
        return new EmpresaDtos.MetricaResposta(atual.getOrDefault(tipo, 0L), anterior.getOrDefault(tipo, 0L));
    }
}
