package edu.k2htm.clientHelper;

public class DuplicateUserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateUserException() {
		super("Duplicate user");
	}
}
