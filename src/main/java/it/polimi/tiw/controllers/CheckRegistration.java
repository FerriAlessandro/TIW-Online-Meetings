package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/Registration")
public class CheckRegistration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;
	
	public void init() throws ServletException{
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		this.connection = ConnectionHandler.getConnection(servletContext);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String repeatPassword = request.getParameter("repeat_password");
		String email = request.getParameter("email");
		UserDAO userDAO = new UserDAO(connection);
		final Pattern VALID_EMAIL_ADDRESS_REGEX = 
			    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
		String path;
		boolean registration;
		
		if (username == null || username.isEmpty() || email == null ||
				email.isEmpty() || password == null || password.isEmpty() 
				|| repeatPassword == null || repeatPassword.isEmpty()) {
			
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("registrationErrorMsg", "Empty fields. Please fill out all of the fields before submitting");
			path = "/index.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		if (!matcher.matches()) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("registrationErrorMsg", "e-mail address is not valid. Please insert a valid e-mail address");
			path = "/index.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		if (!password.equals(repeatPassword)) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("registrationErrorMsg", "Fields password and repeat password do not match");
			path = "/index.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		
		
		
		try {
			registration = userDAO.checkRegistration(username, password, email);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Internal Database Error1");
			return;
		}
		
		
		if (registration == true) {
			User user;
			try {
				user = userDAO.checkCredentials(username, password);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Internal Database Error");
				return;
			}
			request.getSession().setAttribute("user", user);
			path = getServletContext().getContextPath() + "/HomePage";
			response.sendRedirect(path);
		}
		else {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("registrationErrorMsg", "Username or email already in use");
			path = "/index.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}