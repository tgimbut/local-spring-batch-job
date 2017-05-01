package com.tgimbut.springbatch.service;

public class NoAlcoholException extends Exception {

    public NoAlcoholException() {
        super("No alcohol detected!");
    }
}
