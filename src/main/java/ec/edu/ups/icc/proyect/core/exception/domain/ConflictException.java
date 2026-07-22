package ec.edu.ups.icc.proyect.core.exception.domain;

import ec.edu.ups.icc.proyect.core.exception.base.ApplicationException;
import org.springframework.http.HttpStatus;

public class ConflictException extends ApplicationException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, "CONFLICT", message);
    }

    public ConflictException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}