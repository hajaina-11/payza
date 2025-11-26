package io.granix.transaction.resource;

import io.granix.transaction.dto.request.MultiCurrencyTransactionRequest;
import io.granix.transaction.dto.response.TransactionResponse;
import io.granix.transaction.service.MultiCurrencyTransactionService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/transactions/multi-currency")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MultiCurrencyTransactionResource {

    @Inject
    MultiCurrencyTransactionService transactionService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/transfer")
    @RolesAllowed({"USER", "ADMIN"})
    public Map<String, Object> createMultiCurrencyTransfer(MultiCurrencyTransactionRequest request) {
        var response = new HashMap<String, Object>();
        try {
            UUID userId = UUID.fromString(jwt.getClaim("userId").toString());

            TransactionResponse transaction = transactionService.createMultiCurrencyTransaction(request, userId);

            response.put("status", "success");
            response.put("message", "Transaction multi-devises créée avec succès");
            response.put("transaction", transaction);
            response.put("exchange_rate", transaction.exchangeRate);
            response.put("converted_amount", transaction.convertedAmount);

            return response;

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erreur: " + e.getMessage());
            return response;
        }
    }

    @GET
    @Path("/exchange-rate/{fromCurrency}/{toCurrency}")
    @RolesAllowed({"USER", "ADMIN"})
    public Map<String, Object> getExchangeRate(
            @PathParam("fromCurrency") String fromCurrency,
            @PathParam("toCurrency") String toCurrency,
            @QueryParam("amount") @DefaultValue("1") BigDecimal amount) {

        var response = new HashMap<String, Object>();
        try {
            BigDecimal rate = transactionService.getConversionService()
                    .getExchangeRate(fromCurrency, toCurrency);
            BigDecimal convertedAmount = transactionService.getConversionService()
                    .convertAmount(amount, fromCurrency, toCurrency);
            BigDecimal feeEstimate = transactionService.estimateFee(amount, fromCurrency, toCurrency);

            response.put("status", "success");
            response.put("from_currency", fromCurrency);
            response.put("to_currency", toCurrency);
            response.put("exchange_rate", rate);
            response.put("amount", amount);
            response.put("converted_amount", convertedAmount);
            response.put("fee_estimate", feeEstimate);

            return response;

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erreur: " + e.getMessage());
            return response;
        }
    }

    @GET
    @Path("/user")
    @RolesAllowed({"USER", "ADMIN"})
    public Map<String, Object> getUserTransactions(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        var response = new HashMap<String, Object>();
        try {
            UUID userId = UUID.fromString(jwt.getClaim("userId").toString());

            // Implémentation simplifiée - à compléter avec la vraie logique
            response.put("status", "success");
            response.put("message", "Endpoint à implémenter");
            response.put("page", page);
            response.put("size", size);

            return response;

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erreur: " + e.getMessage());
            return response;
        }
    }
}