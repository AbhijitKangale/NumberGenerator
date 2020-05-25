package com.vmware.cpsbu.test.exceptions;

public class ValidationException extends Exception {

	private static final long serialVersionUID = 1650792931213280509L;

	public ValidationException(String message) {
		super(message);
	}
}
