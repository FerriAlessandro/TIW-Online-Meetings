package it.polimi.tiw.beans;

import java.sql.Date;
import java.sql.Time;

public class Meeting{
	
	public static final int MAX_PARTECIPANTS = 5;
	private int id;
	private String title;
	private int id_organizer;
	//private String organizer; //Organizer's username
	private Date meetingDate;
	private Time startingTime;
	private int meetingDuration; //Duration in minutes
	
	
	public Meeting(int id, String title, Date date, Time time, int duration) {
		this.id = id;
		this.title = title;
		this.meetingDate = date;
		this.startingTime = time;
		this.meetingDuration = duration;
	}
	
	public Meeting() {
	
	}
	
	
	public int getID() {
		return id;
	}
	
	/*public String getOrganizer() {
		return organizer;
	}*/
	
	public String getTitle() {
		return title;
	}
	
	public Date getDate() {
		return meetingDate;
	}
	
	public Time getTime() {
		return startingTime;
	}
	
	public int getDuration() {
		return meetingDuration;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	/*public void setOrganizer(String organizer) {
		this.organizer = organizer;
	}*/
	
	public int getOrganizerId() {
		return this.id_organizer;
	}
	
	public void setOrganizerId(int id_organizer) {
		this.id_organizer = id_organizer;
	}

	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setDate(Date date) {
		this.meetingDate = date;
	}
	public void setStartingTime(Time time) {
		this.startingTime = time;
	}
	public void setDuration(int minutes) {
		this.meetingDuration = minutes;
	}
}