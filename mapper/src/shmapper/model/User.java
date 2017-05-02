package shmapper.model;

import java.util.List;

/* Represents a User of the Application. */
public class User extends SerializableObject {
	private static final long		serialVersionUID	= 7348481957757176169L;
	private String					login;
	private String					password;
	private static List<User>		users;

	public User(String login, String pword) {
		this.login = login;
		this.password = pword;
		// users.add(this);
		System.out.println("New: " + this);
	}

	/* Validates login and password and returns the related User, if exists. */
	public static User validate(String login, String pword) {
		for (User user : users) {
			if (user.login.equals(login) && user.password.equals(pword))
				return user;
		}
		return null;
	}

	public String getLogin() {
		return login;
	}

	@Override
	public String toString() {
		return "User: " + login;
	}

	public static void setList(List<User> list) {
		users = list;
	}

}