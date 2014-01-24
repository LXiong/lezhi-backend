package exceptions;

public class UpdateArticleException extends RuntimeException {
	
	public static UpdateArticleException ARTICLE_NOT_FOUND = new UpdateArticleException("article not found");

	public UpdateArticleException() {
		super();
	}

	public UpdateArticleException(String message, Throwable cause) {
		super(message, cause);
	}

	public UpdateArticleException(String message) {
		super(message);
	}

	public UpdateArticleException(Throwable cause) {
		super(cause);
	}
	
}
