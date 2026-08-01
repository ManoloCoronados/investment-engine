package com.investmentengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling /** activa la funcion de scheduler
 */

public class ExchangeSimulatorApplication {


    public static void main(String[] args) {
        SpringApplication.run(ExchangeSimulatorApplication.class, args);

    }
}