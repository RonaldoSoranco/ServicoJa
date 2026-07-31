package com.servicoja.api.categoria;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Lista de categorias de servicos")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaDtos.CategoriaResposta> listarAtivas() {
        return categoriaService.listarAtivas();
    }

    @GetMapping("/todas")
    public List<CategoriaDtos.CategoriaResposta> listarTodas() {
        return categoriaService.listarTodas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaDtos.CategoriaResposta criar(@Valid @RequestBody CategoriaDtos.CategoriaRequest requisicao) {
        return categoriaService.criar(requisicao);
    }

    @PutMapping("/{id}")
    public CategoriaDtos.CategoriaResposta atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody CategoriaDtos.CategoriaRequest requisicao) {
        return categoriaService.atualizar(id, requisicao);
    }

    @PatchMapping("/{id}/atividade")
    public CategoriaDtos.CategoriaResposta alterarAtividade(@PathVariable Long id,
                                                            @RequestParam boolean ativa) {
        return categoriaService.alterarAtividade(id, ativa);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
    }
}
