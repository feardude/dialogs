package ru.smax.social.network;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class DialogsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DialogsApplication.class, args);
    }
}
