package com.example.shipment_tracking_system.exception;

public class InvalidStatusTransitionException extends RuntimeException{
    public InvalidStatusTransitionException(String message){
        super(message);
    }
}
