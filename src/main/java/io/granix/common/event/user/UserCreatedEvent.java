package io.granix.common.event.user;

import java.util.UUID;

public record UserCreatedEvent(UUID userId, String msisdnEncrypted, String emailEncrypted) {
}