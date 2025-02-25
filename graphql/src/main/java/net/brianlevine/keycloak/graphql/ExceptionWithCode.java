package net.brianlevine.keycloak.graphql;

public class ExceptionWithCode extends RuntimeException {
    private ErrorCode code;

    public ExceptionWithCode(String message) {
        super(message);
    }

    public ExceptionWithCode(String message, Throwable cause) {
        super(message, cause);
    }

    public ExceptionWithCode(String message, Throwable cause, ErrorCode code) {
        super(message, cause);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }
}
