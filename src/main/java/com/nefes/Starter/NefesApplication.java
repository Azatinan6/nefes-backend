package com.nefes.Starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.nefes.Starter", 
    "Config", 
    "Controller", 
    "Service", 
    "Repository", 
    "Entity", 
    "DTO"
})
// Sihirli Dokunuş 2: Veritabanı dosyalarının (Repository ve Entity) yerini gösteriyoruz!
@EnableJpaRepositories(basePackages = {"Repository"})
@EntityScan(basePackages = {"Entity"})
public class NefesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NefesApplication.class, args);
    }
}