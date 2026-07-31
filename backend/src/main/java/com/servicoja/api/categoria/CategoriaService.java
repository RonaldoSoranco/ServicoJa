package com.servicoja.api.categoria;

import com.servicoja.dominio.categoria.Categoria;
import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.infra.excecao.NegocioException;
import com.servicoja.infra.excecao.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final EmpresaRepository empresaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, EmpresaRepository empresaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDtos.CategoriaResposta> listarAtivas() {
        return categoriaRepository.findAllByAtivaTrueOrderByNomeAsc().stream()
                .map(this::converter)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaDtos.CategoriaResposta> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::converter)
                .toList();
    }

    @Transactional
    public CategoriaDtos.CategoriaResposta criar(CategoriaDtos.CategoriaRequest requisicao) {
        if (categoriaRepository.existsByNomeIgnoreCase(requisicao.nome().trim())) {
            throw new NegocioException("Ja existe uma categoria com este nome.");
        }
        Categoria categoria = new Categoria();
        categoria.setNome(requisicao.nome().trim());
        categoria.setDescricao(requisicao.descricao());
        categoria.setIcone(requisicao.icone());
        return converter(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaDtos.CategoriaResposta atualizar(Long id, CategoriaDtos.CategoriaRequest requisicao) {
        Categoria categoria = obter(id);
        categoria.setNome(requisicao.nome().trim());
        categoria.setDescricao(requisicao.descricao());
        categoria.setIcone(requisicao.icone());
        return converter(categoriaRepository.save(categoria));
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = obter(id);
        if (empresaRepository.existsByCategoriaId(id)) {
            throw new NegocioException("Categoria em uso por empresas e nao pode ser excluida.");
        }
        categoriaRepository.delete(categoria);
    }

    @Transactional
    public CategoriaDtos.CategoriaResposta alterarAtividade(Long id, boolean ativa) {
        Categoria categoria = obter(id);
        categoria.setAtiva(ativa);
        return converter(categoriaRepository.save(categoria));
    }

    private Categoria obter(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria nao encontrada."));
    }

    private CategoriaDtos.CategoriaResposta converter(Categoria categoria) {
        return new CategoriaDtos.CategoriaResposta(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getIcone(),
                Boolean.TRUE.equals(categoria.getAtiva()));
    }
}
