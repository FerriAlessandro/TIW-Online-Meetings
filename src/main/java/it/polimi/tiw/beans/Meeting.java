package it.polimi.tiw.beans;

import java.sql.Date;
import java.sql.Time;

public class Meeting{
	
	public static final int MAX_PARTECIPANTS = 5;
	private int id;
	private String title;
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
	
	public int getID() {
		return id;
	}
	
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
}