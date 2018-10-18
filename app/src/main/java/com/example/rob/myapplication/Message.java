package com.example.rob.myapplication;

public class Message {


    private String message;
    private String fromAddress;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public boolean isEmpty() {
        if (message.isEmpty()){
            return true;
        }
        return false;

    }
}
