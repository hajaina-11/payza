package io.granix.user.resource;

import io.granix.common.event.user.UserCreatedEvent;
import io.granix.user.dto.request.CompleteUserRegisterRequest;
import io.granix.user.service.UserService;
import io.granix.user.dto.request.UserRegisterRequest;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Path("/user")
public class UserRegisterResource {

    @Inject
    UserService service;

    @Inject
    private Event<UserCreatedEvent> userCreated;

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> register(UserRegisterRequest request) {
        var response = new HashMap<String, Object>();
        try {
            var user = service.register(
                    request.msisdn,
                    request.password,
                    request.RGPDOption
            );

            System.out.println("User registered: "+user.toString());

            userCreated.fireAsync(new UserCreatedEvent(
                    user.id,
                    user.msisdnEncrypted,
                    user.emailEncrypted
            ));

            response.put("message", "User Registered Successfully!");
            response.put("userId", user.id);

            return response;
        } catch (InvalidAlgorithmParameterException
                 | InvalidKeyException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | NoSuchAlgorithmException
                 | BadPaddingException
                 | IllegalArgumentException
                 | InterruptedException e) {
            System.out.println("Error during registration: " + e.getMessage());

            response.put("message", e.getMessage());
            return response;
        }
    }

    //temporaire pour notification
    @POST
    @Path("/register-complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerComplete(CompleteUserRegisterRequest request) {
        try {
            var user = service.registerCompleteUser(request);

            var response = Map.of(
                    "message", "User Registered Successfully with complete profile!",
                    "userId", user.id.toString(),
                    "userName", user.firstname + " " + user.lastname
            );

            return Response.ok(response).build();

        } catch (Exception e) {
            return Response.status(400).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }
}
