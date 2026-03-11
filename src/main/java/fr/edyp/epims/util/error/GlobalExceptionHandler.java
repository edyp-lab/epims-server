/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.edyp.epims.util.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EpimServerException.class)
    public ResponseEntity<ErrorResponse> handleStudyException(EpimServerException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            ex.getErrorCode().getCode(),
            ex.getErrorCode().getMessage(),
            ex.getDetails(),
            request.getDescription(false).replace("uri=", "")
        );
        
        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        String message = ex.getMessage();
        String details = null;
        EpimsErrorCode errorCode = EpimsErrorCode.UNKNOWN_ERROR;

        // Check for specific constraint violations
        if (message != null) {
            if (message.contains("_nomenclature_title_key")) {
                errorCode = EpimsErrorCode.DUPLICATE_NOMENCLATURE;
                details = "A study with this nomenclature title already exists";
            } else if (message.contains("_title_key")) {
                errorCode = EpimsErrorCode.DUPLICATE_TITLE;
                details = "A study with this title already exists";
            } else if (message.contains("duplicate key")) {
                details = "Duplicate key constraint violation";
            }
        }

        ErrorResponse error = new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                details,
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {

        logger.error(" Generic exception in service: ", ex);

        ErrorResponse error = new ErrorResponse(
                EpimsErrorCode.UNKNOWN_ERROR.getCode(),
                EpimsErrorCode.UNKNOWN_ERROR.getMessage(),
                "An unexpected error occurred. Please contact support.",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private HttpStatus determineHttpStatus(EpimsErrorCode errorCode) {
        switch (errorCode) {
            case PROJECT_NOT_FOUND:
            case ACTOR_NOT_FOUND:
            case CONTACT_NOT_FOUND:
            case STUDY_NOT_FOUND:
            case ACQUISITION_NOT_FOUND:
            case PROGRAM_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case INVALID_STUDY_DATA:
            case DUPLICATE_NOMENCLATURE:
            case DUPLICATE_TITLE:
                return HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED_ACCESS:
                return HttpStatus.FORBIDDEN;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
