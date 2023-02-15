package com.igatn.extranet.rest.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class ApiError {
    private int code;
    private String message;
    
    public ApiError(String message){
        this.code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.message = message;
    }
}