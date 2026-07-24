package ec.edu.ups.icc.proyect.core.exception.domain;

import ec.edu.ups.icc.proyect.core.exception.base.ApplicationException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApplicationException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "FORBIDDEN_OPERATION", message);
    }
}