package it.consumer.finder.rest;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class Response {

    private boolean success;
    private String message;

}
