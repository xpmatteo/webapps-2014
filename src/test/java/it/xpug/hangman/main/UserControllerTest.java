package it.xpug.hangman.main;

import static org.junit.Assert.*;

import java.util.*;

import org.jmock.*;
import org.jmock.integration.junit4.*;
import org.junit.*;

public class UserControllerTest {

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery();
	WebResponse response = context.mock(WebResponse.class);
	WebRequest request = context.mock(WebRequest.class);

	Random random = new Random(444);
	UserBase users = new UserBase(random);
	UserController controller = new UserController(users);

	@Test
	public void itTakesANameToCreateAUser() throws Exception {
		UserBase users = new UserBase();
		UserController controller = new UserController(users);

		context.checking(new Expectations() {{
			allowing(request).getRequestURI(); will(returnValue("http://whatever/users"));
			allowing(request).getMethod(); will(returnValue("post"));
			allowing(request).getParameter(with(any(String.class))); will(returnValue(null));
			oneOf(response).validationError("Parameter 'name' is required");
		}});

		controller.handleRequest(request, response);
	}

	@Test
	public void itTakesAPasswordToCreateAUser() throws Exception {
		UserBase users = new UserBase();
		UserController controller = new UserController(users);

		context.checking(new Expectations() {{
			allowing(request).getRequestURI(); will(returnValue("http://whatever/users"));
			allowing(request).getMethod(); will(returnValue("post"));
			allowing(request).getParameter(with("name")); will(returnValue("gino"));
			allowing(request).getParameter(with(any(String.class))); will(returnValue(null));
			oneOf(response).validationError("Parameter 'password' is required");
		}});

		controller.handleRequest(request, response);
	}

	@Test
	public void createsAUser() throws Exception {

		context.checking(new Expectations() {{
			allowing(request).getRequestURI(); will(returnValue("http://whatever/users"));
			allowing(request).getMethod(); will(returnValue("post"));
			allowing(request).getParameter(with("name")); will(returnValue("gino"));
			allowing(request).getParameter(with("password")); will(returnValue("secr3t"));
			oneOf(response).redirect("/users/3cb54a30");
		}});

		controller.handleRequest(request, response);

		assertTrue("user created", users.contains(new UserId("3cb54a30"), "secr3t"));
	}

	@Test
	public void returnListOfPrisoners() throws Exception {
		final UserId userId = new UserId("1234");
		users.add(userId, "name", "s3cret");
		context.checking(new Expectations() {{
			allowing(request).getRequestURI(); will(returnValue("/users/1234/prisoners"));
			allowing(request).getMethod(); will(returnValue("get"));
			allowing(request).getParameter(with("password")); will(returnValue("s3cret"));
			allowing(request).getUserId(); will(returnValue(userId));
			oneOf(response).put("items", new ArrayList<Prisoner>());
			oneOf(response).put("url", "/users/1234/prisoners");
		}});
		controller.handleRequest(request, response);
	}

	@Test
	public void createAPrisoner() throws Exception {
		final UserId userId = new UserId("1234");
		users.add(userId, "name", "s3cret");
		context.checking(new Expectations() {{
			allowing(request).getRequestURI(); will(returnValue("/users/1234/prisoners"));
			allowing(request).getMethod(); will(returnValue("post"));
			allowing(request).getParameter(with("password")); will(returnValue("s3cret"));
			allowing(request).getUserId(); will(returnValue(userId));
			oneOf(response).redirect("/users/1234/prisoners/3cb54a30");
		}});
		controller.handleRequest(request, response);

		assertEquals(1, users.findPrisoners(userId).size());
	}

	@Test
	public void guess() throws Exception {
		final UserId userId = new UserId("1234");
		users.add(userId, "name", "s3cret");
		users.addPrisoner(userId, new Prisoner("abc123", "word"));

		context.checking(new Expectations() {{
			allowing(request).getRequestURI(); will(returnValue("/users/1234/prisoners/abc123"));
			allowing(request).getMethod(); will(returnValue("post"));
			allowing(request).getParameter(with("password")); will(returnValue("s3cret"));
			allowing(request).getParameter(with("guess")); will(returnValue("x"));
			allowing(request).getUserId(); will(returnValue(userId));
			allowing(request).getPrisonerId(); will(returnValue("abc123"));
			oneOf(response).redirect("/users/1234/prisoners/abc123");
		}});
		controller.handleRequest(request, response);

		assertEquals(17, users.findPrisoner(userId, "abc123").getGuessesRemaining());
	}

	@Test
	public void guessValidation() throws Exception {
		final UserId userId = new UserId("1234");
		users.add(userId, "name", "s3cret");
		users.addPrisoner(userId, new Prisoner("abc123", "word"));

		context.checking(new Expectations() {{
			allowing(request).getRequestURI(); will(returnValue("/users/1234/prisoners/abc123"));
			allowing(request).getMethod(); will(returnValue("post"));
			allowing(request).getParameter(with("password")); will(returnValue("s3cret"));
			allowing(request).getParameter(with("guess")); will(returnValue(null));
			allowing(request).getUserId(); will(returnValue(userId));
			oneOf(response).validationError("Parameter 'guess' is required");
		}});
		controller.handleRequest(request, response);

		assertEquals(18, users.findPrisoner(userId, "abc123").getGuessesRemaining());
	}

	@Test
	public void guessForbidden() throws Exception {
		final UserId userId = new UserId("1234");
		users.add(userId, "name", "s3cret");
		users.addPrisoner(userId, new Prisoner("abc123", "word"));

		context.checking(new Expectations() {{
			allowing(request).getRequestURI(); will(returnValue("/users/1234/prisoners/abc123"));
			allowing(request).getMethod(); will(returnValue("post"));
			allowing(request).getParameter(with("password")); will(returnValue(null));
			allowing(request).getParameter(with("guess")); will(returnValue("x"));
			allowing(request).getUserId(); will(returnValue(userId));
			allowing(request).getPrisonerId(); will(returnValue("abc123"));
			oneOf(response).forbidden(with(any(String.class)));
		}});
		controller.handleRequest(request, response);

		assertEquals(18, users.findPrisoner(userId, "abc123").getGuessesRemaining());
	}

}