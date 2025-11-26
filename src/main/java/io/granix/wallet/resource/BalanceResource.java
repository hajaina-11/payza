package io.granix.wallet.resource;

import io.granix.wallet.dto.response.BalanceResponse;
import io.granix.wallet.service.WalletInformationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path(value = "/wallets/balance")
public class BalanceResource {
    @Inject
    WalletInformationService service;

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed("ADMIN")
    @Path(value = "/")
    @Produces(value = MediaType.APPLICATION_JSON)
    public List<BalanceResponse> list()
    {
        var wallets = service.getAllWallet();

        return wallets.stream()
                .map(w -> new BalanceResponse(
                        w.id,
                        w.balance,
                        w.availableBalance,
                        w.currencyCode,
                        w.ownerId
                ))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/user/{userId}")
    @RolesAllowed({"USER", "ADMIN"})
    public List<BalanceResponse> listByUserId(@PathParam("userId") String userId)
    {
        System.out.println("====================================Get Single Wallet====================================");
        var user = jwt.getClaim("userId");

        System.out.println("UserId From JWT: "+user);
        System.out.println("UserId From URL: "+userId);
        System.out.println("Compare these two: "+user.equals(userId));

        if(!user.equals(userId))
            throw new NotAuthorizedException("Action not permitted");

        var wallets = service.searchByUser(UUID.fromString(userId));

        System.out.println("====================================End of Get Single Wallet====================================");
        return wallets.stream()
                .map(w -> new BalanceResponse(
                        w.id,
                        w.balance,
                        w.availableBalance,
                        w.currencyCode,
                        w.ownerId
                ))
                .collect(Collectors.toList());
    }

    @GET
    @Path("{walletId}/user/{userId}")
    @RolesAllowed({"USER", "ADMIN"})
    public BalanceResponse getWalletByUserId(
            @PathParam("userId") String userId,
            @PathParam("walletId") String walletId
    ) {
        System.out.println("====================================Get Single Wallet====================================");
        var user = jwt.getClaim("userId");

        System.out.println("UserId From JWT: "+user);
        System.out.println("UserId From URL: "+userId);
        System.out.println("Compare these two: "+user.equals(userId));

        if(!user.equals(userId))
            throw new NotAuthorizedException("Action not permitted");

        var wallet = service.getWalletByUserId(UUID.fromString(userId), UUID.fromString(walletId));

        System.out.println("====================================End of Get Single Wallet====================================");
        return new BalanceResponse(
                wallet.id,
                wallet.balance,
                wallet.availableBalance,
                wallet.currencyCode,
                wallet.ownerId
        );
    }
}
