package br.com.tst.controller;

import br.com.tst.model.Funcionario;
import br.com.tst.service.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("funcionarios", funcionarioService.listar());
        return "funcionarios/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("funcionario", new Funcionario());
        return "funcionarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("funcionario", funcionarioService.buscar(id));
        return "funcionarios/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(
            @Valid @ModelAttribute("funcionario") Funcionario funcionario,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "funcionarios/formulario";
        }

        try {
            boolean nova = funcionario.getId() == null;
            funcionarioService.salvar(funcionario);
            redirectAttributes.addFlashAttribute(
                "sucesso",
                nova
                    ? "Funcionario cadastrado com sucesso."
                    : "Funcionario atualizado com sucesso."
            );
            return "redirect:/funcionarios";
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("nome", "funcionario.duplicada", exception.getMessage());
            return "funcionarios/formulario";
        }
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id,
                          RedirectAttributes redirectAttributes) {
        try {
            funcionarioService.excluir(id);
            redirectAttributes.addFlashAttribute(
                "sucesso",
                "Funcionario excluída com sucesso."
            );
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }

        return "redirect:/funcionarios";
    }
}
