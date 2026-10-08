package com.servicoja.api.auth;

import com.servicoja.api.avaliacao.AvaliacaoService;
import com.servicoja.api.empresa.EmpresaService;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.favorito.FavoritoRepository;
import com.servicoja.dominio.notificacao.NotificacaoRepository;
import com.servicoja.dominio.seguranca.TokenRecuperacaoRepository;
import com.servicoja.dominio.seguranca.TokenRefreshRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import com.servicoja.infra.excecao.LimiteExcedidoException;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.seguranca.LimitadorRequisicoes;
import com.servicoja.infra.servico.LogService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

/**
 * Exclusao da conta pelo proprio usuario (direito previsto na LGPD e exigido pelas lojas de apps).
 * Os dados pessoais sao apagados ou anonimizados; permanecem so os registros que a lei obriga a
 * guardar (assinaturas, pagamentos e registros de acesso), sem identificar a pessoa.
 */
@Service
public class ExclusaoContaService {

    private static final int MAXIMO_TENTATIVAS = 5;
    private static final Duration JANELA_TENTATIVAS = Duration.ofMinutes(15);

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final EmpresaService empresaService;
    private final AvaliacaoService avaliacaoService;
    private final FavoritoRepository favoritoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final TokenRefreshRepository tokenRefreshRepository;
    private final TokenRecuperacaoRepository tokenRecuperacaoRepository;
    private final PasswordEncoder codificador;
    private final LimitadorRequisicoes limitador;
    private final LogService logService;

    public ExclusaoContaService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            EmpresaService empresaService,
            AvaliacaoService avaliacaoService,
            FavoritoRepository favoritoRepository,
            NotificacaoRepository notificacaoRepository,
            TokenRefreshRepository tokenRefreshRepository,
            TokenRecuperacaoRepository tokenRecuperacaoRepository,
            PasswordEncoder codificador,
            LimitadorRequisicoes limitador,
            LogService logService) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.empresaService = empresaService;
        this.avaliacaoService = avaliacaoService;
        this.favoritoRepository = favoritoRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.tokenRefreshRepository = tokenRefreshRepository;
        this.tokenRecuperacaoRepository = tokenRecuperacaoRepository;
        this.codificador = codificador;
        this.limitador = limitador;
        this.logService = logService;
    }

    /**
     * Exige a senha atual: um token de acesso roubado nao basta para apagar a conta de alguem.
     */
    @Transactional
    public void excluir(Usuario usuario, AuthDtos.ExcluirContaRequest requisicao) {
        if (usuario.getPerfil() == Perfil.ADMIN) {
            throw new NegocioException("Contas de administrador nao podem ser excluidas pelo aplicativo.");
        }
        if (!limitador.permitido("excluir-conta:" + usuario.getId(), MAXIMO_TENTATIVAS, JANELA_TENTATIVAS)) {
            throw new LimiteExcedidoException("Muitas tentativas. Aguarde 15 minutos e tente novamente.");
        }
        if (!codificador.matches(requisicao.senha(), usuario.getSenha())) {
            throw new NegocioException("Senha incorreta.");
        }

        Long usuarioId = usuario.getId();
        empresaRepository.findByUsuarioIdAndExcluidaEmIsNull(usuarioId)
                .forEach(empresaService::removerDaPlataforma);
        avaliacaoService.removerAvaliacoesDoUsuario(usuario);
        favoritoRepository.deleteByUsuarioId(usuarioId);
        notificacaoRepository.deleteByUsuarioId(usuarioId);
        tokenRefreshRepository.deleteByUsuarioId(usuarioId);
        tokenRecuperacaoRepository.deleteByUsuarioId(usuarioId);

        usuario.anonimizar(codificador.encode(UUID.randomUUID().toString()));
        usuarioRepository.save(usuario);
        logService.registrar(usuario, "CONTA_EXCLUIDA", "Conta excluida pelo proprio usuario.");
    }
}
