package com.animenotice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AnimeNoticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnimeNoticeApplication.class, args);
    }
}
