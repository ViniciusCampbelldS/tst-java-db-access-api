package br.com.tst.controller;

import br.com.tst.form.EpiForm;
import br.com.tst.service.FuncionarioService;
import br.com.tst.service.EpiService;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/epis")
public class EpiController {

    private final EpiService epiService;
    private final FuncionarioService funcionarioService;

    public EpiController(EpiService epiService,
                         FuncionarioService funcionarioService) {
        this.epiService = epiService;
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("epis", epiService.listar());
        return "epis/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        prepararFormulario(model, new EpiForm());
        return "epis/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        EpiForm form = EpiForm.from(epiService.buscar(id));
        prepararFormulario(model, form);
        return "epis/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(
            @Valid @ModelAttribute("epiForm") EpiForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            prepararFormulario(model, form);
            return "epis/formulario";
        }

        epiService.salvar(form);
        redirectAttributes.addFlashAttribute(
            "sucesso",
            form.getId() == null
                ? "Epi cadastrado com sucesso."
                : "Epi atualizado com sucesso."
        );

        return "redirect:/epis";
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id,
                          RedirectAttributes redirectAttributes) {
        epiService.excluir(id);
        redirectAttributes.addFlashAttribute(
            "sucesso",
            "Epi excluído com sucesso."
        );
        return "redirect:/epis";
    }

    private void prepararFormulario(Model model, EpiForm form) {
        model.addAttribute("epiForm", form);
        model.addAttribute("funcionarios", funcionarioService.listar());
    }
}
