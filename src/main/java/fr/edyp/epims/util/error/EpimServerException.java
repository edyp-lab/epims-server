package fr.edyp.epims.util.error;

public class EpimServerException extends RuntimeException {
    private final EpimsErrorCode errorCode;
    private final String details;

    public EpimServerException(EpimsErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public EpimServerException(EpimsErrorCode errorCode, String details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    public EpimsErrorCode getErrorCode() { return errorCode; }
    public String getDetails() { return details; }
}
