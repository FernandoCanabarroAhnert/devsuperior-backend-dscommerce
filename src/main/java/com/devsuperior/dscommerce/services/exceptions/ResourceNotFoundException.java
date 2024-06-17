package com.devsuperior.dscommerce.services.exceptions;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(Long id){
        super("Recurso não encontrado! Id: " + id);
    }

    public ResourceNotFoundException(String msg){
        super(msg);
    }
}
