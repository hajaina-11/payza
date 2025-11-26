package io.granix.transaction.infrastructure.sandbox;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

@Path("/sandbox/blockchain")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SandboxBlockchainResource {

    private final ConcurrentMap<String, Integer> confirmations = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public SandboxBlockchainResource() {
        // augmenter les confirmations progressivement
        scheduler.scheduleAtFixedRate(() -> confirmations.forEach((k,v) -> confirmations.put(k, Math.min(v + 1, 999))),
                2, 2, TimeUnit.SECONDS);
    }

    @POST
    @Path("/send")
    public Response sendTx(Map<String,Object> body, @QueryParam("simulateFailure") @DefaultValue("false") boolean simulateFailure) {
        if (simulateFailure) {
            return Response.status(500).entity(Map.of("success", false, "error", "Simulated chain failure")).build();
        }
        String txHash = generateFakeHash();
        confirmations.put(txHash, 0);
        return Response.ok(Map.of("success", true, "txHash", txHash, "confirmations", 0)).build();
    }

    @GET
    @Path("/status/{txHash}")
    public Response status(@PathParam("txHash") String txHash) {
        Integer conf = confirmations.get(txHash);
        if (conf == null) {
            return Response.status(404).entity(Map.of("error", "tx not found")).build();
        }
        String status = conf >= 12 ? "COMPLETED" : "PENDING";
        return Response.ok(Map.of("txHash", txHash, "confirmations", conf, "status", status)).build();
    }

    private String generateFakeHash() {
        return "0x" + Long.toHexString(Instant.now().toEpochMilli()) + Integer.toHexString((int)(Math.random()*100000));
    }
}
