package com.example.demo.exception;

public class DepartmentInUseException extends RuntimeException{
    public DepartmentInUseException(String message){
        super(message);
    }
}
