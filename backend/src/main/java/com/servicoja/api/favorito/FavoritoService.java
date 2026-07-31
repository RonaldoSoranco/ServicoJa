package com.servicoja.api.favorito;

import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.favorito.Favorito;
import com.servicoja.dominio.favorito.FavoritoRepository;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.infra.PageResposta;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final EmpresaRepository empresaRepository;

    public FavoritoService(FavoritoRepository favoritoRepository, EmpresaRepository empresaRepository) {
        this.favoritoRepository = favoritoRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public FavoritoDtos.MensagemResposta alternar(Usuario usuario, Long empresaId) {
        Optional<Favorito> existente = favoritoRepository.findByUsuarioIdAndEmpresaId(usuario.getId(), empresaId);
        if (existente.isPresent()) {
            favoritoRepository.delete(existente.get());
            return new FavoritoDtos.MensagemResposta("Empresa removida dos favoritos.");
        }
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada."));
        Favorito favorito = new Favorito();
        favorito.setUsuario(usuario);
        favorito.setEmpresa(empresa);
        favoritoRepository.save(favorito);
        return new FavoritoDtos.MensagemResposta("Empresa adicionada aos favoritos.");
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
