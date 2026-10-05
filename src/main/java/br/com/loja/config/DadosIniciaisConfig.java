package br.com.loja.config;

import br.com.loja.model.Categoria;
import br.com.loja.repository.CategoriaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DadosIniciaisConfig {

    @Bean
    CommandLineRunner carregarCategorias(CategoriaRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Categoria(
                    "Informática",
                    "Computadores, acessórios e periféricos"
                ));
                repository.save(new Categoria(
                    "Escritório",
                    "Materiais e equipamentos de escritório"
                ));
            }
        };
    }
}
