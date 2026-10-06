package br.com.tst.config;

import br.com.tst.model.Funcionario;
import br.com.tst.repository.FuncionarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DadosIniciaisConfig {

    @Bean
    CommandLineRunner carregarFuncionarios(FuncionarioRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Funcionario(
                    "Informática",
                    "Computadores, acessórios e periféricos"
                ));
                repository.save(new Funcionario(
                    "Escritório",
                    "Materiais e equipamentos de escritório"
                ));
            }
        };
    }
}
