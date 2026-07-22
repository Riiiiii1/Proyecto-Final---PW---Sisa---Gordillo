package ec.edu.ups.icc.proyect.core.exception.domain;

import ec.edu.ups.icc.proyect.core.exception.base.ApplicationException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApplicationException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }
}