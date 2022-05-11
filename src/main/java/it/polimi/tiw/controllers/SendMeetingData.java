package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet ("/SendMeetingData")

public class SendMeetingData extends HttpServlet {
	
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
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int attempts = 0;
		List<User> selectedUsers = new ArrayList<>(); //Empty arrayList for the first time the form is sent to the user
		List<User> users = new ArrayList<>();
		Meeting meeting = new Meeting();
		UserDAO userDao = new UserDAO(connection);
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'hh:mm"); 
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		String path = "/WEB-INF/selectParticipants.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		try {
			meeting.setDate(formatter.parse(request.getParameter("date")));
			Date currentDate = new Date(); //Get today's date
			if(meeting.getDate().getTime() < currentDate.getTime()) { //If today's date (in ms) is greater than the meeting date 
				ctx.setVariable("errorDate", "You can't create a meeting in the past! Please select a valid date");
				path = getServletContext().getContextPath() + "/HomePage";
				response.sendRedirect(path);
				return; 
				
			}
		} catch (ParseException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error while parsing the date");
			return;
		}
		meeting.setTitle(request.getParameter("title"));
		meeting.setDuration(Integer.parseInt(request.getParameter("duration")));
		meeting.setOrganizerId(user.getID());
		meeting.setOrganizerName(user.getUserName());
		
		try {
			users = userDao.GetRegisteredUsers(user);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Internal Database Error");
			return;
		}
		
		ctx.setVariable("users", users);
		ctx.setVariable("selectedUsers", selectedUsers);
		ctx.setVariable("attempts", attempts);
		ctx.setVariable("meeting", meeting);
		templateEngine.process(path, ctx, response.getWriter());
		
		
	}
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
}
