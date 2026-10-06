package br.com.tst.config;

import br.com.tst.model.Epi;
import br.com.tst.model.Funcionario;
import br.com.tst.repository.FuncionarioRepository;
import br.com.tst.repository.EpiRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class EpisIniciaisConfig {

    @Bean
    CommandLineRunner carregarEpis(EpiRepository epiRepository,
                                       FuncionarioRepository funcionarioRepository) {
        return args -> {
            if (epiRepository.count() == 0) {
                Epi epiInformatica = new Epi(
                    "NR11",
                    "Segurança para o transporte, a movimentação, a armazenagem e o manuseio de materiais.",
                    new BigDecimal("11.11"),
                    16,
                    List.of()
                );
                Epi epiEscritorio = new Epi(
                    "NR13",
                    "Norma Regulamentadora do Ministério do Trabalho e Emprego (MTE) que estabelece os requisitos mínimos para a gestão da integridade estrutural de caldeiras, vasos de pressão, tubulações e tanques metálicos de armazenamento.",
                    new BigDecimal("14.44"),
                    12,
                    List.of()
                );

                epiRepository.saveAll(List.of(epiInformatica, epiEscritorio));
            }
        };
    }

    private Funcionario buscarOuCriarFuncionario(FuncionarioRepository repository,
                                               String nome,
                                               String descricao) {
        return repository.findByNomeIgnoreCase(nome)
            .orElseGet(() -> repository.save(new Funcionario(nome, descricao)));
    }
}
