package com.fip.appointmentapi.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService
{

    public String getHelloMessage()
    {
        return "Hello, Spring Boot is working!";
    }
}