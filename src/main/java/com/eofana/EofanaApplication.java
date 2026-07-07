package com.eofana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * T-B-117 : @EnableScheduling active le support des tâches planifiées
 * Spring, nécessaire pour AutomaticTransferService (@Scheduled sur
 * generateWeeklyTransfers() / generateMonthlyTransfers()).
 */
@SpringBootApplication
@EnableScheduling
public class EofanaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EofanaApplication.class, args);
	}

}
