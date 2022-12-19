/**
 * Copyright (C) 2019 Koninklijke Philips N.V, Inc. All rights reserved
 * @Owner Koninklijke Philips N.V.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.philips.poc.error;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@RestController
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

	/**
	 * Method to handle Genecric exception
	 * 
	 * @param ex
	 * @param request
	 * @return ErrorResponse
	 */
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setMessage(FmsErrorCodes.UNKNOWN_ERROR.getErrorMessage());
		errorResponse.setCode(FmsErrorCodes.UNKNOWN_ERROR.getErrorCode());
		errorResponse.setSeverity(FmsErrorCodes.UNKNOWN_ERROR.getSeverity());
		errorResponse.setDescription(FmsErrorCodes.UNKNOWN_ERROR.getDescription());
		errorResponse.setCause(ex.getMessage());
		LOGGER.error("::::::::::::::::::::: Exception ::::::::::::::::::::: " + ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Method to handle FirmwareManagementException
	 * 
	 * @param ex
	 * @param request
	 * @return ErrorResponse
	 * @throws StarterErrorException
	 */
	@ExceptionHandler({ FirmwareManagementException.class })
	public ResponseEntity<ErrorResponse> handleFirmwareManagementException(FirmwareManagementException ex,
			WebRequest request) {
		ErrorResponse firmwareManagementErrorResponse = getFirmwareManagementErrorDetails(ex);
		LOGGER.error(":::::::::::::::::::::FirmwareManagementException ::::::::::::::::::::: {}:{}",
				ex.getErrors().getErrorCode(), ex.getErrors().getCause());
		HttpStatus status = null;
		if (ex.getHttpStatus() != null) {
			status = ex.getHttpStatus();
		} else {
			status = HttpStatus.BAD_REQUEST;
		}

		return new ResponseEntity<>(firmwareManagementErrorResponse, new HttpHeaders(), status);
	}

	/**
	 * Provides handling for standard Spring exceptions.
	 * 
	 * @param ex      the target exception
	 * @param request the current request
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException methodArgumentNotValidException, HttpHeaders headers, HttpStatus status,
			WebRequest request) {
		LOGGER.error(":::::::::::::::::::::MethodArgumentNotValidException Exception ::::::::::::::::::::: "
				+ methodArgumentNotValidException.getMessage());
		BindingResult result = methodArgumentNotValidException.getBindingResult();
		List<FieldError> fieldErrorsList = result.getFieldErrors();
		return new ResponseEntity<>(processFieldErrors(fieldErrorsList), HttpStatus.BAD_REQUEST);

	}

	/**
	 * Method to map spring validation error to Rsm validation error response
	 * 
	 * @param fieldErrorsList
	 * @return
	 */
	private ErrorResponse processFieldErrors(List<FieldError> fieldErrorsList) {
		List<ValidationMessage> validaionMessageList = new ArrayList<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setMessage(FmsErrorCodes.INVALID_REQUEST.getErrorMessage());
		errorResponse.setCode(FmsErrorCodes.INVALID_REQUEST.getErrorCode());
		errorResponse.setSeverity("Fatal");
		errorResponse.setDescription(FmsErrorCodes.INVALID_REQUEST.getDescription());
		errorResponse.setCause(FmsErrorCodes.INVALID_REQUEST.getCause());

		LOGGER.error(":::::::::::::ValidationErrorDTO::::::::map error message to ::::::::::::::::::::: ");
		for (FieldError fieldError : fieldErrorsList) {
			ValidationMessage validaionMessage = new ValidationMessage();
			validaionMessage.setField(fieldError.getField());
			validaionMessage.setErrorMessage(fieldError.getDefaultMessage());
			validaionMessageList.add(validaionMessage);
		}
		errorResponse.setValidation(validaionMessageList);
		return errorResponse;
	}

	/**
	 * Create the Error Response
	 * 
	 * @param ex
	 * @return ErrorResponse
	 * @throws StarterErrorException
	 */
	private ErrorResponse getFirmwareManagementErrorDetails(FirmwareManagementException ex) {
		ErrorResponse fmsErrorDetails = new ErrorResponse();
		fmsErrorDetails.setCode(ex.getErrors().getErrorCode());
		fmsErrorDetails.setMessage(ex.getErrors().getErrorMessage());
		fmsErrorDetails.setSeverity(ex.getErrors().getSeverity());
		if (StringUtils.isEmpty(ex.getArg())) {
			fmsErrorDetails.setDescription(String.format(ex.getErrors().getDescription(), StringUtils.EMPTY));
			fmsErrorDetails.setCause(ex.getErrors().getCause());
		} else {
			fmsErrorDetails.setDescription(String.format(ex.getErrors().getDescription(), ex.getArg()));
			fmsErrorDetails.setCause(String.format(ex.getErrors().getCause(), ex.getArg()));
		}
		return fmsErrorDetails;

	}

}
