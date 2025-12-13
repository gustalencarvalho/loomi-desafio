package com.ecommerce.order_processing_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrderProcessingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderProcessingSystemApplication.class, args);
	}

}
