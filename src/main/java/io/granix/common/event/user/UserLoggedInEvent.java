package io.granix.common.event.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserLoggedInEvent(
        UUID userId,
        String ipAddress,
        String userAgent,
        LocalDateTime loginTime,
        boolean success // true = connexion réussie, false = échec
) {}