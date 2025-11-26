package io.granix.transaction.infrastructure.external;

import io.granix.transaction.port.ExternalPaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@ApplicationScoped
public class SandboxPaymentGateway implements ExternalPaymentGateway {

    private final Random rnd = new Random();

    @Override
    public String deposit(UUID walletId, BigDecimal amount, String currency, String requestId, String outcome) throws Exception {
        simulateDelay(outcome);
        if ("failure".equals(outcome) && rnd.nextInt(100) < 30) {
            throw new RuntimeException("Simulated deposit failure");
        }
        if ("insufficient".equals(outcome)) {
            throw new RuntimeException("Simulated insufficient (deposit impossible)");
        }
        return "SIMBANK-" + Instant.now().toEpochMilli() + "-" + Math.abs(rnd.nextInt(99999));
    }

    @Override
    public String withdraw(UUID walletId, BigDecimal amount, String currency, String requestId, String outcome) throws Exception {
        simulateDelay(outcome);
        if ("insufficient".equals(outcome)) {
            throw new RuntimeException("Simulated insufficient funds on external system");
        }
        if ("failure".equals(outcome) && rnd.nextInt(100) < 20) {
            throw new RuntimeException("Simulated withdrawal gateway error");
        }
        return "SIMPAYOUT-" + Instant.now().toEpochMilli() + "-" + Math.abs(rnd.nextInt(99999));
    }

    private void simulateDelay(String outcome) {
        if ("delay".equals(outcome)) {
            try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
        }
    }
}
