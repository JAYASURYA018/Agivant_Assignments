package com.bookpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookPulseApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" 🌟 BookPulse - Smart Library Operations Platform is Running! 🌟 ");
        System.out.println(" 📖 Web Application: http://localhost:8080");
        System.out.println(" 🗄️ H2 Database Console: http://localhost:8080/h2-console");
        System.out.println(" ✨ AI Companion: Sky is ready to recommend books!");
        System.out.println("==================================================================");
    }
}
