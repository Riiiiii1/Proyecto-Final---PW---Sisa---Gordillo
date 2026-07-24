package ec.edu.ups.icc.proyect.core.exception.domain;

import ec.edu.ups.icc.proyect.core.exception.base.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends ApplicationException {
    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", message);
    }
}