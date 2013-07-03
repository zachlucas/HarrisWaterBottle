package edu.psu.database;

import java.util.Date;

public class Gulp {
	private Date date;
	private double amount;
	
	public Gulp() {
		this(new Date(), 0.0);
	}
	
	public Gulp(Date date, double amount) {
		this.date = date;
		this.amount = amount;
	}
	
	public Date getDate() {
		return date;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setAmount(double amount) {
		this.amount = amount;
	}
}
