package ru.antisessa.CarRefuels;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AnalyzingCarRefuelsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyzingCarRefuelsApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}
}
