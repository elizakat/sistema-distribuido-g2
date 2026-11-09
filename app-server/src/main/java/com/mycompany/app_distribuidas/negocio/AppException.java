package com.mycompany.app_distribuidas.negocio;

public class AppException extends RuntimeException {
 private final String code;public AppException(String code,String message){super(message);this.code=code;}public String getCode(){return code;}
}