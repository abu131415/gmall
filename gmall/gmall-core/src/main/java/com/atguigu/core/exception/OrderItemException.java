package com.atguigu.core.exception;

public class OrderItemException extends RuntimeException {
    public OrderItemException() {
        super();
    }

    public OrderItemException(String message) {
        super(message);
    }
}
