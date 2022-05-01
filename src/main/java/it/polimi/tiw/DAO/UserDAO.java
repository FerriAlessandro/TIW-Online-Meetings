package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.User;

public class UserDAO{
	
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public User checkCredentials(String userName, String password)throws SQLException {
		
		PreparedStatement preparedStatement;
		ResultSet queryResult;
		String query = "SELECT id, username FROM user WHERE username = ? AND password = ?";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, userName);
		preparedStatement.setString(2, password);


		queryResult = preparedStatement.executeQuery();

		if(!queryResult.isBeforeFirst()) //Credentials not found
			return null;

		else {
			queryResult.next(); //get the query's result
			User user = new User();
			user.setID(queryResult.getInt("id"));
			user.setUserName(queryResult.getString("username"));
			return user;
		}
			
	}
	
	public User checkRegistration(String userName, String password, String repeat_password, String email) throws SQLException{
		
		PreparedStatement preparedStatement, registrationPreparedStatement;
		ResultSet queryResult, registrationQueryResult;
		User user;
		
		String checkQuery = "SELECT id FROM user WHERE username = ? OR email = ?"; //if someone is already registered with the same username or email
																			       //this query will return a result
		String registrationQuery = "INSERT INTO user (username, psw, email) VALUES (?, ?, ?)";
		
		if(!password.equals(repeat_password))
			throw new PasswordMatchException(); //TODO PasswordMatchException
		
		preparedStatement= connection.prepareStatement(checkQuery);
		preparedStatement.setString(1,  userName);
		preparedStatement.setString(2,  email);
		
		queryResult = preparedStatement.executeQuery();
		if(queryResult.isBeforeFirst()) //if the query returned a result 
			return null;
		else {
			
			registrationPreparedStatement = connection.prepareStatement(registrationQuery);
			registrationPreparedStatement.setString(1, userName);
			registrationPreparedStatement.setString(2, password);
			registrationPreparedStatement.setString(3,  email);
			
			registrationQueryResult = registrationPreparedStatement.executeQuery();
			
			return checkCredentials(userName, password); //Return the new user 	
			
		}
		
		
	}
	
	public List<User> GetRegisteredUsers (User user) throws SQLException{ //'user' is the current user that needs to select other users to invite to the meeting
		
		PreparedStatement preparedStatement;
		ResultSet queryResult;
		ArrayList<User> users = new ArrayList<>();
		String query = "SELECT username FROM user WHERE username != ?";
		preparedStatement = connection.prepareStatement(query);
	
		String userName = user.getUserName(); //The username of the user who is creating a meeting, he can't invite himself! We remove him
											  //from the list of possible users to invite
		
		preparedStatement.setString(1, userName);
		queryResult = preparedStatement.executeQuery();
		
		if(!queryResult.isBeforeFirst()) //If zero users are registered 
			return null;
		
		else {
			
			while(queryResult.next()) {
				
				User tmp = new User();
				tmp.setID(queryResult.getInt("id"));
				tmp.setUserName(queryResult.getString("username"));
				users.add(tmp);
			}
			return users;
		}
	}
	
}