package com.ampgo.demo.listener;

import com.ampgo.demo.event.UserRegisteredEvent;
import com.ampgo.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredEventListener {

    private final NotificationService notificationService;

    @Async("eventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {

        log.info("Received user registered event for email: {}", event.getEmail());
        try {

            log.info("Sending verification code {} to email: {}",
                    event.getVerificationCode(), event.getEmail());

            notificationService.send(event.getEmail(), event.getVerificationCode());

            log.info("Verification email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", event.getEmail(), e);
        }
    }
}
