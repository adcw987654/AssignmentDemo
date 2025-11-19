package com.ampgo.demo.service;

public interface NotificationService {
    void send(String email, String verificationCode);
}