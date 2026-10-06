package br.com.tst.config;

import br.com.tst.model.Epi;
import br.com.tst.repository.EpiRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class EpisIniciaisConfig {

    @Bean
    CommandLineRunner carregarEpis(EpiRepository epiRepository) {
        return args -> {
            if (epiRepository.count() == 0) {
                Epi epiCapacete = new Epi(
                    "12345",
                    "Capacete de segurança",
                    LocalDate.parse("2030-12-31"),
                    List.of()
                );
                Epi epiOculos = new Epi(
                    "67890",
                    "Óculos de proteção",
                    LocalDate.parse("2031-06-30"),
                    List.of()
                );

                epiRepository.saveAll(List.of(epiCapacete, epiOculos));
            }
        };
    }

}
