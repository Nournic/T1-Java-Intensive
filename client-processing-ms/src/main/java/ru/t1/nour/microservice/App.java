package ru.t1.nour.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@SpringBootApplication
public class App {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        //SpringApplication.run(App.class, args);

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(256);

    }


}
