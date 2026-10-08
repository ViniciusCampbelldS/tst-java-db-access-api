package br.com.tst.config;

import br.com.tst.model.NR;
import br.com.tst.repository.NRRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class NRIniciaisConfig {

    private static final List<String> NRS_INICIAIS = List.of(
            "NR 01 - Disposições gerais",
            "NR 02 - Inspeção prévia (Revogada)",
            "NR 03 - Comissão Interna de Prevenção de Acidentes (CIPA)",
            "NR 04 - Serviços Especializados em Engenharia de Segurança e em Medicina do Trabalho (SESMT)",
            "NR 05 - Comissão Interna de Prevenção de Acidentes",
            "NR 06 - Equipamentos de Proteção Individual (EPI)",
            "NR 07 - Programa de Controle Médico de Saúde Ocupacional (PCMSO)",
            "NR 08 - Edificações",
            "NR 09 - Programa de Prevenção de Riscos Ambientais (PPRA)",
            "NR 10 - Segurança em Instalações e Serviços em Eletricidade",
            "NR 11 - Transporte, Movimentação, Armazenagem e Manuseio de Materiais",
            "NR 12 - Segurança no Trabalho em Máquinas e Equipamentos",
            "NR 13 - Caldeiras, Vasos de Pressão e Tubulações",
            "NR 14 - Fornos",
            "NR 15 - Atividades e Operações Insalubres",
            "NR 16 - Atividades e Operações Perigosas",
            "NR 17 - Ergonomia",
            "NR 18 - Condições e Meio Ambiente de Trabalho na Indústria da Construção",
            "NR 19 - Explosivos",
            "NR 20 - Segurança e Saúde no Trabalho com Inflamáveis e Combustíveis",
            "NR 21 - Trabalho a Céu Aberto",
            "NR 22 - Mineração",
            "NR 23 - Proteção Contra Incêndios",
            "NR 24 - Condições Sanitárias e de Conforto nos Locais de Trabalho",
            "NR 25 - Resíduos Industriais",
            "NR 26 - Sinalização de Segurança",
            "NR 27 - Registro Profissional do Técnico de Segurança (Revogada)",
            "NR 28 - Fiscalização e Penalidades",
            "NR 29 - Segurança e Saúde no Trabalho Portuário",
            "NR 30 - Segurança e Saúde no Trabalho Aquaviário",
            "NR 31 - Segurança e Saúde no Trabalho na Agricultura, Pecuária, Silvicultura, Exploração Florestal e Aquicultura",
            "NR 32 - Segurança e Saúde no Trabalho em Serviços de Saúde",
            "NR 33 - Segurança e Saúde no Trabalho em Espaços Confinados",
            "NR 34 - Condições e Meio Ambiente de Trabalho na Indústria de Construção Naval",
            "NR 35 - Trabalho em Altura",
            "NR 36 - Segurança e Saúde no Trabalho em Empresas de Abate e Processamento de Carnes e Derivados",
            "NR 37 - Plataformas de Petróleo",
            "NR 38 - Limpeza Urbana e Manejo de Resíduos Sólidos"
    );

    @Bean
    CommandLineRunner carregarNrs(NRRepository repository) {
        return args -> NRS_INICIAIS.stream()
                .filter(nome -> !repository.existsByNome(nome))
                .map(NR::new)
                .forEach(repository::save);
    }
}
