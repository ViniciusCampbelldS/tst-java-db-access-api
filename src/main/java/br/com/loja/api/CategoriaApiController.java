package br.com.loja.api;

import br.com.loja.dto.CategoriaRequest;
import br.com.loja.dto.CategoriaResponse;
import br.com.loja.model.Categoria;
import br.com.loja.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaApiController {

    private final CategoriaService categoriaService;

    public CategoriaApiController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaService.listar().stream().map(CategoriaResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscar(@PathVariable Long id) {
        return CategoriaResponse.from(categoriaService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> cadastrar(
            @Valid @RequestBody CategoriaRequest request) {
        Categoria categoria = categoriaService.salvar(
            new Categoria(request.nome(), request.descricao())
        );
        CategoriaResponse response = CategoriaResponse.from(categoria);
        return ResponseEntity
            .created(URI.create("/api/categorias/" + categoria.getId()))
            .body(response);
    }

    @PutMapping("/{id}")
    public CategoriaResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {
        Categoria categoria = categoriaService.buscar(id);
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        return CategoriaResponse.from(categoriaService.salvar(categoria));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
    }
}
