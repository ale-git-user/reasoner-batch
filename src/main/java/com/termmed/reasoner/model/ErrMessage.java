package com.termmed.reasoner.model;

public class ErrMessage {

	int number;
	public ErrMessage(int number, String message) {
		super();
		this.number = number;
		this.message = message;
	}
	String message;
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
}
