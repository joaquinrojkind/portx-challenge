package com.portx.payment.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PaymentsExceptionHandler {

	@ExceptionHandler({ RuntimeException.class })
	public ResponseEntity handleRuntimeException(RuntimeException exception) {
		ApiErrorDto errorPayload = ApiErrorDto.builder()
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.code(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.message("Internal error while processing the payment")
				.build();
		return ResponseEntity
			.status(errorPayload.getStatus())
			.body(errorPayload);
	}
}
