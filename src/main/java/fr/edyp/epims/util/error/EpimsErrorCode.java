package fr.edyp.epims.util.error;

public enum EpimsErrorCode {
    PROJECT_NOT_FOUND("EPIMS_001", "Project not found"),
    ACTOR_NOT_FOUND("EPIMS_002", "Actor not found"),
    CONTACT_NOT_FOUND("EPIMS_003", "Contact not found"),
    STUDY_NOT_FOUND("EPIMS_004", "Study not found"),
    STUDY_DIRECTORY_CREATION_FAILED("EPIMS_005", "Failed to create study directory"),
    INVALID_STUDY_DATA("EPIMS_006", "Invalid study data provided"),
    PROGRAM_NOT_FOUND("EPIMS_007", "Program not found"),
    UNAUTHORIZED_ACCESS("EPIMS_008", "Unauthorized access to study"),
    DUPLICATE_NOMENCLATURE("EPIMS_009", "Nomenclature already exists"),
    DUPLICATE_TITLE("EPIMS_010", "Title already exists"),
    PROJECT_DIRECTORY_CREATION_FAILED("EPIMS_011", "Failed to create project directory"),
    PROGRAM_DIRECTORY_CREATION_FAILED("EPIMS_012", "Failed to create program directory"),

    UNKNOWN_ERROR("EPIMS_999", "Unkonwn error");

    private final String code;
    private final String message;

    EpimsErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
