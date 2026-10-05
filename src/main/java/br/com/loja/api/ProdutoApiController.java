package br.com.loja.api;
import br.com.loja.dto.ProdutoRequest;
import br.com.loja.dto.ProdutoResponse;
import br.com.loja.form.ProdutoForm;
import br.com.loja.model.Produto;
import br.com.loja.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoApiController {

    private final ProdutoService produtoService;

    public ProdutoApiController(ProdutoService produtoService){
        this.produtoService = produtoService;
    }

    // Listar todos Produtos
    @GetMapping
    public List<ProdutoResponse> list(){
        return produtoService.listar().stream().map(ProdutoResponse::from).toList();
    }

    // Procurar produto por ID
    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable Long id){
        return ProdutoResponse.from(produtoService.buscar(id));
    }

    //  Create/	Submits new data
    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrar(
            @Valid @RequestBody ProdutoRequest request){

        Produto produto = produtoService.salvar(converterParaForm(null, request));

        ProdutoResponse response = ProdutoResponse.from(produto);

        return ResponseEntity.created(URI.create("/api/produtos/" + produto.getId())).body(response);
    }

    //	Update/	Replaces an entire resource
    @PutMapping("/{id}")
    public ProdutoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequest request
    ){
        Produto produto = produtoService.salvar(converterParaForm(id, request));

        return ProdutoResponse.from(produto);
    }



    //	Removes data permanently
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id){
        produtoService.excluir(id);
    }


    private ProdutoForm converterParaForm (Long id, ProdutoRequest produtoRequest){
        ProdutoForm form = new ProdutoForm();
        form.setId(id);
        form.setNome(produtoRequest.nome());
        form.setDescricao(produtoRequest.descricao());
        form.setPreco(produtoRequest.preco());
        form.setQuantidade(produtoRequest.quantidade());
        form.setCategoriaIds(produtoRequest.categoriaIds());
        return form;
    }
}
