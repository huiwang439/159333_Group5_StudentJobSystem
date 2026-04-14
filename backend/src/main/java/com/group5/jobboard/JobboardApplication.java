package com.group5.jobboard;

import com.group5.jobboard.entity.User;
import com.group5.jobboard.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JobboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobboardApplication.class, args);
	}

	@Bean
	public org.springframework.boot.CommandLineRunner initAdmin(UserRepository userRepository) {
		return args -> {
			if (userRepository.findByRole("admin").isEmpty()) {

				User admin = new User();
				admin.setEmail("admin@test.com");
				admin.setPassword("123456");
				admin.setFullName("Admin User");
				admin.setRole("admin");
				admin.setPhone("9999999999");
				admin.setAccountStatus("active");

				userRepository.save(admin);

				System.out.println("Admin account created");
			}
		};
	}
}