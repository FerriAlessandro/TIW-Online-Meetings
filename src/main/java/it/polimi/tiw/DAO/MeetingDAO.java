package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.User;

public class MeetingDAO{
	
	private Connection connection;
	
	public MeetingDAO(Connection connection) {
		this.connection = connection;
		
	}
	
	public ArrayList<Meeting> getMeetingsByOwner(User user) throws SQLException{
		
		PreparedStatement preparedStatement;
		String query = "SELECT title, meeting_date, starting_time, minutes FROM meetings WHERE id_organizer = ?";
		int id = user.getID();
		ResultSet resultSet;
		ArrayList<Meeting> meetings = new ArrayList<>();
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, id);
		resultSet = preparedStatement.executeQuery();
		if(!resultSet.isBeforeFirst())
			return null;
		else {
			while(resultSet.next()) {
				
				Meeting tmp = new Meeting();
				tmp.setTitle(resultSet.getString("title"));
				tmp.setDate(resultSet.getDate("meeting_date"));
				tmp.setStartingTime(resultSet.getTime("starting_time"));
				tmp.setDuration(resultSet.getInt("minutes"));
				tmp.setID(resultSet.getInt("id"));
				meetings.add(tmp);
			}
			
			return meetings;
		}

	}
}