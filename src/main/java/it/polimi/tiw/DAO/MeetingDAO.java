package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.User;

public class MeetingDAO{
	
	private Connection connection;
	
	public MeetingDAO(Connection connection) {
		this.connection = connection;
		
	}
	
	public ArrayList<Meeting> getMeetingsByOwner(User user) throws SQLException{
		//TODO AGGIUNGERE CHECK SCADENZA RIUNIONE
		PreparedStatement preparedStatement;
		String query = "SELECT * FROM meetings WHERE id_organizer = ?";
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
	
	
	public ArrayList<Meeting> GetUserInvitations(User user)throws SQLException{
		//TODO AGGIUNGERE CHECK SCADENZA RIUNIONE
		PreparedStatement preparedStatement;
		ResultSet resultSet;
		ArrayList<Meeting> meetings = new ArrayList<>();
		String query = "Select * FROM meetings JOIN invitations ON meetings.id = invitations.id_meeting"
				+ "WHERE invitations.id_user = ?";
		int id = user.getID();
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
	
	public void createMeeting(List<User> guests, Meeting meeting) throws SQLException {
		PreparedStatement meetingPreparedStatement;
		PreparedStatement invitationPreparedStatement;
		PreparedStatement meetingIdPreparedStatement;
		
		ResultSet meetingIdResultSet;
		String addMeetingQuery = "INSERT INTO meetings (id_organizer,title,meeting_date,starting_time,minutes) VALUES (?, ?, ?, ?, ?)";
		String addInvitationQuery = "INSERT INTO invitations (id_user, id_meeting) VALUES (?, ?)";
		String getMeetingIdQuery = "SELECT max(id) FROM meetings";
		int meetingID;
		int id_organizer = meeting.getOrganizerId();
		String title = meeting.getTitle();
		Date date = meeting.getDate();
		Time time = meeting.getTime();
		int duration = meeting.getDuration();
		
		connection.setAutoCommit(false);
		try {
			meetingPreparedStatement = connection.prepareStatement(addMeetingQuery);
			meetingPreparedStatement.setInt(1, id_organizer);
			meetingPreparedStatement.setString(2, title);
			meetingPreparedStatement.setDate(3, date);
			meetingPreparedStatement.setTime(4, time);
			meetingPreparedStatement.setInt(5, duration);
			meetingPreparedStatement.executeQuery();
			
			meetingIdPreparedStatement = connection.prepareStatement(getMeetingIdQuery);
			meetingIdResultSet = meetingIdPreparedStatement.executeQuery();
			
			if(!meetingIdResultSet.isBeforeFirst())
				throw new SQLException();
			
			else {
				meetingIdResultSet.next();
				meetingID = meetingIdResultSet.getInt("id");
			}
			
			invitationPreparedStatement = connection.prepareStatement(addInvitationQuery);
			
			for (User user : guests) {
				invitationPreparedStatement.setInt(1,user.getID());
				invitationPreparedStatement.setInt(2,meetingID);
				invitationPreparedStatement.addBatch();
			}	
			
			invitationPreparedStatement.executeBatch();
			
		} catch (SQLException e) {
			connection.rollback(); // if update 1 OR 2 fails, roll back all work
			throw e;
			
		} finally {
			connection.setAutoCommit(true);
		}

	}
		
			
}
	
	