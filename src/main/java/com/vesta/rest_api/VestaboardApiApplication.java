package com.vesta.rest_api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling
public class VestaboardApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(VestaboardApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			// System.err.println("Spring Boot provided beans:");
			System.err.println("Starting API");

			// String[] beanNames = ctx.getBeanDefinitionNames();
			// Arrays.sort(beanNames);

			// for (String name : beanNames) {
			// System.err.println(name);
			// }
			;
		};
	}

	// Enabling CORS
	@Bean
	public WebMvcConfigurer corsConfiguration() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/request_song").allowedOrigins("http://localhost:3000");
				registry.addMapping("/current").allowedOrigins("http://localhost:3000");
				registry.addMapping("/get_auth_url").allowedOrigins("http://localhost:3000");
				registry.addMapping("/send_auth_token").allowedOrigins("http://localhost:3000");
				registry.addMapping("/auth_status").allowedOrigins("http://localhost:3000");
				registry.addMapping("/*").allowedOrigins("http://localhost:3000");
			}
		};
	}
}
