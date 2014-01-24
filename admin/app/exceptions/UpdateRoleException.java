package exceptions;

public class UpdateRoleException extends RuntimeException {

	public UpdateRoleException() {
	}

	public UpdateRoleException(String message) {
		super(message);
	}

	public UpdateRoleException(Throwable cause) {
		super(cause);
	}

	public UpdateRoleException(String message, Throwable cause) {
		super(message, cause);
	}

}
