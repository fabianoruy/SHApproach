package shmapper.applications;

/* Represents the exceptions occurred during the mapping process (for matches). */
public class MatchException extends Exception {
	private static final long serialVersionUID = 1L;

	public MatchException(String message) {
		super(message);
	}

}
