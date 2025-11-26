package io.granix.notification.application.event;

import io.granix.notification.application.service.NotificationOrchestrator;
import io.granix.wallet.event.WalletCreatedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;

@ApplicationScoped
public class NotificationEventListener {

    @Inject
    NotificationOrchestrator orchestrator;

    public void handleWalletCreated(@ObservesAsync WalletCreatedEvent event) {
        System.out.println("🎯 Notification: Wallet créé pour " + event.getUserId());

        //  Implémenter la notification de création de wallet
        // orchestrator.sendWalletCreatedNotification(event.getUserId(), event.getWalletId());
    }
}