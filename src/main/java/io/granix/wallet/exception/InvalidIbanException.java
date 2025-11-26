package io.granix.wallet.exception;

public class InvalidIbanException extends RuntimeException{
    public InvalidIbanException(String message){
        super(message);
    }
}
