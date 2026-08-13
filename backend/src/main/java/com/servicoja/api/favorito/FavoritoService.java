package com.servicoja.api.favorito;

import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.favorito.Favorito;
import com.servicoja.dominio.favorito.FavoritoRepository;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.infra.PageResposta;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final EmpresaRepository empresaRepository;

    public FavoritoService(FavoritoRepository favoritoRepository, EmpresaRepository empresaRepository) {
        this.favoritoRepository = favoritoRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public FavoritoDtos.MensagemResposta favoritar(Usuario usuario, Long empresaId) {
        if (favoritoRepository.existsByUsuarioIdAndEmpresaId(usuario.getId(), empresaId)) {
            throw new NegocioException("Esta empresa ja esta nos seus favoritos.");
        }
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada."));
        if (!Boolean.TRUE.equals(empresa.getAprovada())) {
            throw new NegocioException("A empresa precisa ser aprovada para ser favoritada.");
        }
        Favorito favorito = new Favorito();
        favorito.setUsuario(usuario);
        favorito.setEmpresa(empresa);
        favoritoRepository.save(favorito);
        return new FavoritoDtos.MensagemResposta("Empresa adicionada aos favoritos.");
    }

    @Transactional
    public FavoritoDtos.MensagemResposta desfavoritar(Usuario usuario, Long empresaId) {
        favoritoRepository.findByUsuarioIdAndEmpresaId(usuario.getId(), empresaId)
                .ifPresent(favoritoRepository::delete);
        return new FavoritoDtos.MensagemResposta("Empresa removida dos favoritos.");
    }

    @Transactional(readOnly = true)
    public PageResposta<FavoritoDtos.FavoritoResposta> listar(Usuario usuario, int pagina, int tamanho) {
        return PageResposta.de(favoritoRepository
                .findByUsuarioId(usuario.getId(),
                        PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), 50)))
                .map(favorito -> converter(favorito.getEmpresa())));
    }

    @Transactional(readOnly = true)
    public FavoritoDtos.EstadoResposta verificarEstado(Usuario usuario, Long empresaId) {
        return new FavoritoDtos.EstadoResposta(
                favoritoRepository.existsByUsuarioIdAndEmpresaId(usuario.getId(), empresaId));
    }

    private FavoritoDtos.FavoritoResposta converter(Empresa empresa) {
        return new FavoritoDtos.FavoritoResposta(
                empresa.getId(),
                empresa.getNome(),
                empresa.getLogoUrl(),
                empresa.getCidade(),
                empresa.getUf(),
                Boolean.TRUE.equals(empresa.getPremiumAtivo()),
                Boolean.TRUE.equals(empresa.getDestaque()),
                empresa.getMediaAvaliacoes(),
                empresa.getTotalAvaliacoes());
    }
}
