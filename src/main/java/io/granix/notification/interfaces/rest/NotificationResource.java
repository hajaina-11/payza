package io.granix.notification.interfaces.rest;

import io.granix.notification.infrastructure.persistence.NotificationEntity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @GET
    @Path("/user/{userId}")
    @RolesAllowed({"ADMIN"})
    public List<Object> getUserNotifications(@PathParam("userId") String userId) {
        UUID userUuid = UUID.fromString(userId);

        return NotificationEntity.find("userId", userUuid).list()
                .stream()
                .map(entity -> {
                    NotificationEntity notificationEntity = (NotificationEntity) entity;
                    return Map.of(
                            "id", notificationEntity.id,
                            "userId", notificationEntity.userId,
                            "type", notificationEntity.type,
                            "channel", notificationEntity.channel,
                            "title", notificationEntity.title,
                            "status", notificationEntity.status,
                            "createdAt", notificationEntity.createdAt
                    );
                })
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{notificationId}")
    @RolesAllowed({"ADMIN"})
    public Response getNotification(@PathParam("notificationId") String notificationId) {
        UUID notificationUuid = UUID.fromString(notificationId);

        return NotificationEntity.findByIdOptional(notificationUuid)
                .map(entity -> {
                    NotificationEntity notificationEntity = (NotificationEntity) entity;
                    Object response = Map.of(
                            "id", notificationEntity.id,
                            "userId", notificationEntity.userId,
                            "type", notificationEntity.type,
                            "channel", notificationEntity.channel,
                            "title", notificationEntity.title,
                            "body", notificationEntity.body,
                            "status", notificationEntity.status,
                            "createdAt", notificationEntity.createdAt
                    );
                    return Response.ok(response).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}