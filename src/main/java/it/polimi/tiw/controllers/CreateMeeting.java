package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

import it.polimi.tiw.DAO.MeetingDAO;
import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CreateMeeting")
public class CreateMeeting extends HttpServlet{
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
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		
		List<User> registeredUsers = new ArrayList<>();
		List<Integer> selectedUsersID = new ArrayList<>(); //ArrayList of Integers that contain the ID's of the already selected participants
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		String path = "/WEB-INF/selectParticipants.html";
		int attempts;
		
		//if the number of attempts is not a number
		try {
			attempts = Integer.parseInt(request.getParameter("attemptCounter"));
		}catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter type");
			return;
		}
		
		
		Meeting meeting = new Meeting();
		SimpleDateFormat formatter=new SimpleDateFormat("E MMM d HH:mm:ss Z yyyy", Locale.ENGLISH);
		if(request.getParameter("duration").length() == 0 || request.getParameter("duration") == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Meeting duration! Please insert a valid number");
			return;
		}
		
		if(attempts < 0) { //If someone modified the number of attempts client-side
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid number of attempts");
			return;
		}
		
		meeting.setTitle(request.getParameter("title"));
		meeting.setOrganizerId(user.getID());
		//If the duration is not a number
		try {
			meeting.setDuration(Integer.parseInt(request.getParameter("duration")));
		}catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter type");
			return;
		}
		meeting.setOrganizerName(user.getUserName());
		try {
			meeting.setDate(formatter.parse(request.getParameter("date")));
		}catch(ParseException e ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Date!");
			return;
		}
		

		Date currentDate = new Date(); //Get today's date
		//If today's date is greater than the meeting date or if the parameters are not valid
		if(meeting.getDate().getTime() < currentDate.getTime()){ 
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You can't create a Meeting in the past! Please select a valid date");
			return;
		}

		if(meeting.getTitle().length()==0 || meeting.getTitle() == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty Title field! Please insert a Title");
			return;
		}

		if(meeting.getDuration() <= 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Duration! Please insert a valid number");
			return;
		}

		
			
		
		//IF THE USER DIDNT SELECT ANY PARTICIPANT
		if(request.getParameterValues("user_id") == null) {
			UserDAO userDAO = new UserDAO(connection);
			try {
				registeredUsers = userDAO.GetRegisteredUsers(user);
			} catch (SQLException e) {
				
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Interal Database Error!");
				e.printStackTrace();
				return;
			}
			ctx.setVariable("users", registeredUsers);
			ctx.setVariable("selectedUsersID", selectedUsersID);
			ctx.setVariable("attempts", attempts);
			ctx.setVariable("meeting", meeting);
			ctx.setVariable("errorMsg", "At least one participants is required!");
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		//extract the selected users from the checkbox
		String[] invitedUsers = request.getParameterValues("user_id");
		
		for(String id : invitedUsers) {
			User u = new User();
			//if the ID is not a number
			try {
				u.setID(Integer.parseInt(id));
			}catch(NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters type");
				return; 
			}
			selectedUsersID.add(u.getID());
		}
		
		
		//if the number of selected users is valid
		if(selectedUsersID.size() <= Meeting.MAX_PARTECIPANTS) {
			MeetingDAO meetingDAO = new MeetingDAO(connection);
			try {
				meetingDAO.createMeeting(selectedUsersID, meeting);
			} catch (SQLException e) {
				
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Interal Database Error!");
				e.printStackTrace();
				return;
			}
			
			path = servletContext.getContextPath() + "/HomePage";
			response.sendRedirect(path);
			return;
		
		}
		
		//if the number of selected users is not valid
		else {
			attempts +=1;
			if(attempts >= 3) {
				path = servletContext.getContextPath()+ "/Cancellation";
				response.sendRedirect(path);
				return;
			}
			UserDAO userDAO = new UserDAO(connection);
			try {
				registeredUsers = userDAO.GetRegisteredUsers(user);
			} catch (SQLException e) {
				
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Interal Database Error!");
				e.printStackTrace();
				return;
			}
			
			
			ctx.setVariable("users", registeredUsers);
			ctx.setVariable("selectedUsersID", selectedUsersID);
			ctx.setVariable("attempts", attempts);
			ctx.setVariable("meeting", meeting);
			ctx.setVariable("errorMsg", "Too many participants selected! You can't create a meeting with more than "+ Meeting.MAX_PARTECIPANTS +" participants!"
					+ " You have " + (3 - attempts) +" attempts left!");
			templateEngine.process(path, ctx, response.getWriter());
			
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