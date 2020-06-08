package com.cigi.iems.exception.advice;

import com.cigi.iems.exception.BaseErrorModel;
import com.cigi.iems.exception.ResourceNotFoundException;
import com.cigi.iems.exception.UserAlreadyInUseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class AuthControllerAdvice {

	@ExceptionHandler(value = UserAlreadyInUseException.class)
	protected ResponseEntity<BaseErrorModel> handleUsernameAlreadyInUseException(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(BaseErrorModel.builder().message(e.getMessage()).build());
	}
	
	@ExceptionHandler(value = ResourceNotFoundException.class)
	protected ResponseEntity<BaseErrorModel> handleUserNotFoundException(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(BaseErrorModel.builder().message(e.getMessage()).build());
	}

}
