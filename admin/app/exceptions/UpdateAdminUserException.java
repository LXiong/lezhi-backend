package exceptions;

public class UpdateAdminUserException extends RuntimeException {

	public UpdateAdminUserException() {
	}

	public UpdateAdminUserException(String message) {
		super(message);
	}

	public UpdateAdminUserException(Throwable cause) {
		super(cause);
	}

	public UpdateAdminUserException(String message, Throwable cause) {
		super(message, cause);
	}

}
