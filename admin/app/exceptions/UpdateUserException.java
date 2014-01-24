package exceptions;

public class UpdateUserException extends RuntimeException {

	public UpdateUserException() {
	}

	public UpdateUserException(String message) {
		super(message);
	}

	public UpdateUserException(Throwable cause) {
		super(cause);
	}

	public UpdateUserException(String message, Throwable cause) {
		super(message, cause);
	}

}
