package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.User;

public class UserDAO{
	
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public User checkCredentials(String userName, String password)throws SQLException {
		
		PreparedStatement preparedStatement;
		ResultSet queryResult;
		String query = "SELECT id, username from user WHERE username = ? AND password = ?";
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
	
}