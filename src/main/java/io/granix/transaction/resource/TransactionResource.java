package io.granix.transaction.resource;

import io.granix.transaction.dto.request.TransactionRequest;
import io.granix.transaction.dto.response.TransactionResponse;
import io.granix.transaction.service.TransactionService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {

    @Inject
    TransactionService transactionService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/")
    @RolesAllowed({"USER", "ADMIN"})
    public Map<String, Object> createTransaction(TransactionRequest request) {
        var response = new HashMap<String, Object>();
        try {
            Object userIdClaim = jwt.getClaim("userId");
            if (userIdClaim == null) {
                response.put("status", "error");
                response.put("message", "Utilisateur non authentifié");
                return response;
            }
            UUID userId = UUID.fromString(userIdClaim.toString());
            TransactionResponse transaction = transactionService.createTransaction(request, userId);

            response.put("status", "success");
            response.put("transaction", transaction);
            response.put("transaction_id", transaction.id);
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erreur lors de la création de la transaction: " + e.getMessage());
            return response;
        }
    }

    @GET
    @Path("/")
    @RolesAllowed({"USER", "ADMIN"})
    public Map<String, Object> getUserTransactions(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        var response = new HashMap<String, Object>();
        try {
            Object userIdClaim = jwt.getClaim("userId");
            if (userIdClaim == null) {
                response.put("status", "error");
                response.put("message", "Utilisateur non authentifié");
                return response;
            }
            java.util.UUID userId = java.util.UUID.fromString(userIdClaim.toString());
            List<TransactionResponse> transactions = transactionService.getUserTransactions(userId, page, size);

            response.put("status", "success");
            response.put("transactions", transactions);
            response.put("count", transactions.size());
            response.put("page", page);
            response.put("size", size);
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erreur lors de la récupération des transactions: " + e.getMessage());
            return response;
        }
    }

    @GET
    @Path("/{transactionId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Map<String, Object> getTransaction(@PathParam("transactionId") UUID transactionId) {
        var response = new HashMap<String, Object>();
        try {
            Object userIdClaim = jwt.getClaim("userId");
            if (userIdClaim == null) {
                response.put("status", "error");
                response.put("message", "Utilisateur non authentifié");
                return response;
            }
            UUID userId = UUID.fromString(userIdClaim.toString());
            TransactionResponse transaction = transactionService.getTransaction(transactionId, userId);

            response.put("status", "success");
            response.put("transaction", transaction);
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erreur lors de la récupération de la transaction: " + e.getMessage());
            return response;
        }
    }
}
