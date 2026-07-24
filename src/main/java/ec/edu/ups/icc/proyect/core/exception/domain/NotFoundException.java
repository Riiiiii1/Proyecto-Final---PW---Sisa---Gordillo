package ec.edu.ups.icc.proyect.core.exception.domain;
import ec.edu.ups.icc.proyect.core.exception.base.ApplicationException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ApplicationException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }
}