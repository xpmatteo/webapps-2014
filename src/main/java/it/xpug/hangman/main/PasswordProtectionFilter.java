package it.xpug.hangman.main;

import it.xpug.hangman.domain.*;

public class PasswordProtectionFilter {

	private UserBase users;
	private boolean shouldContinue = true;

	public PasswordProtectionFilter(UserBase users) {
		this.users = users;
	}

	public void applyTo(WebRequest request, WebResponse response) {
		if (needsPassword(request.getRequestPath()) && passwordDoesNotMatch(request)) {
			forbidden(response);
			shouldContinue = false;
		}
	}

	private boolean needsPassword(String path) {
		return path.matches("^/users/[a-f0-9]+.*");
	}

	private boolean passwordDoesNotMatch(WebRequest request) {
		return !users.contains(request.getUserId(), request.getParameter("password"));
	}

	private void forbidden(WebResponse response) {
		response.forbidden("You don't have the permission to access the requested resource. It is either read-protected or not readable by the server.");
	}

	public boolean shouldContinue() {
		return shouldContinue ;
	}
}
