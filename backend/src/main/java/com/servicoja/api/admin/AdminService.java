package com.servicoja.api.admin;

import com.servicoja.dominio.avaliacao.AvaliacaoRepository;
import com.servicoja.dominio.avaliacao.StatusAvaliacao;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.log.Log;
import com.servicoja.dominio.log.LogRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.PageResposta;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final LogRepository logRepository;

    public AdminService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            AvaliacaoRepository avaliacaoRepository,
            LogRepository logRepository) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.logRepository = logRepository;
    }

    @Transactional(readOnly = true)
    public AdminDtos.EstatisticasResposta estatisticas() {
        return new AdminDtos.EstatisticasResposta(
                usuarioRepository.count(),
                usuarioRepository.findByPerfil(Perfil.CLIENTE).size(),
                empresaRepository.count(),
                empresaRepository.countByPremiumAtivoTrue(),
                empresaRepository.countByAprovadaFalse(),
                avaliacaoRepository.countByStatus(StatusAvaliacao.PENDENTE),
                avaliacaoRepository.countByStatus(StatusAvaliacao.APROVADA),
                avaliacaoRepository.count());
    }

    @Transactional(readOnly = true)
    public PageResposta<AdminDtos.UsuarioAdminResposta> listarUsuarios(int pagina, int tamanho) {
        return PageResposta.de(usuarioRepository
                .findAll(PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), 50)))
                .map(this::converterUsuario));
    }

    @Transactional(readOnly = true)
    public PageResposta<AdminDtos.LogResposta> listarLogs(int pagina, int tamanho) {
        return PageResposta.de(logRepository
                .findAllByOrderByCriadoEmDesc(
                        PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), 50)))
                .map(this::converterLog));
    }

    private AdminDtos.UsuarioAdminResposta converterUsuario(Usuario usuario) {
        return new AdminDtos.UsuarioAdminResposta(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                Boolean.TRUE.equals(usuario.getAtivo()),
                usuario.getCriadoEm());
    }

    private AdminDtos.LogResposta converterLog(Log log) {
        return new AdminDtos.LogResposta(
                log.getId(),
                log.getUsuario() != null ? log.getUsuario().getNome() : null,
                log.getAcao(),
                log.getDetalhes(),
                log.getIp(),
                log.getCriadoEm());
    }
}
