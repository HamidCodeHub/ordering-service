package com.awesomepizza.orderingservice.config;

import com.awesomepizza.orderingservice.model.entity.Pizza;
import com.awesomepizza.orderingservice.repository.PizzaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(PizzaRepository pizzaRepository) {
        return args -> {
            if (pizzaRepository.count() == 0) {
                Pizza margherita = Pizza.builder()
                        .name("Margherita")
                        .description("Pomodoro, mozzarella, basilico")
                        .price(new BigDecimal("8.00"))
                        .available(true)
                        .build();

                Pizza marinara = Pizza.builder()
                        .name("Marinara")
                        .description("Pomodoro, aglio, origano")
                        .price(new BigDecimal("7.00"))
                        .available(true)
                        .build();

                Pizza quattroStagioni = Pizza.builder()
                        .name("Quattro Stagioni")
                        .description("Pomodoro, mozzarella, funghi, prosciutto, carciofi, olive")
                        .price(new BigDecimal("12.00"))
                        .available(true)
                        .build();

                Pizza diavola = Pizza.builder()
                        .name("Diavola")
                        .description("Pomodoro, mozzarella, salame piccante")
                        .price(new BigDecimal("10.00"))
                        .available(true)
                        .build();

                pizzaRepository.saveAll(Arrays.asList(margherita, marinara, quattroStagioni, diavola));
            }
        };
    }
}