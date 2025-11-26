package io.granix.transaction.infrastructure.sandbox;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Path("/sandbox/bank")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SandboxBankResource {

    // Simple API key check (dev only)
    private static final String DEV_API_KEY = "Api-Key 12345";

    @POST
    @Path("/deposit")
    public Response deposit(Map<String,Object> body,
                            @HeaderParam("Authorization") String auth,
                            @HeaderParam("X-Request-ID") String requestId,
                            @QueryParam("outcome") @DefaultValue("success") String outcome) {
        if (!DEV_API_KEY.equals(auth)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("success", false, "error", "invalid api key")).build();
        }
        try {
            UUID walletId = UUID.fromString((String) body.get("walletId"));
            BigDecimal amount = new BigDecimal(body.get("amount").toString());
            String currency = body.getOrDefault("currency", "MGA").toString();
            if ("insufficient".equals(outcome)) {
                return Response.status(422).entity(Map.of("success", false, "error", "insufficient")).build();
            }
            String reference = "SIMBANK-" + System.currentTimeMillis();
            return Response.ok(Map.of("success", true, "reference", reference, "outcome", outcome)).build();
        } catch (Exception e) {
            return Response.status(400).entity(Map.of("success", false, "error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/withdraw")
    public Response withdraw(Map<String,Object> body,
                             @HeaderParam("Authorization") String auth,
                             @HeaderParam("X-Request-ID") String requestId,
                             @QueryParam("outcome") @DefaultValue("success") String outcome) {
        if (!DEV_API_KEY.equals(auth)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("success", false, "error", "invalid api key")).build();
        }
        try {
            UUID walletId = UUID.fromString((String) body.get("walletId"));
            BigDecimal amount = new BigDecimal(body.get("amount").toString());
            if ("insufficient".equals(outcome)) {
                return Response.status(422).entity(Map.of("success", false, "error", "insufficient")).build();
            }
            String reference = "SIMPAYOUT-" + System.currentTimeMillis();
            return Response.ok(Map.of("success", true, "reference", reference, "outcome", outcome)).build();
        } catch (Exception e) {
            return Response.status(400).entity(Map.of("success", false, "error", e.getMessage())).build();
        }
    }
}
