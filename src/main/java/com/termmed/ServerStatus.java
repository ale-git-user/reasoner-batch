package com.termmed;

import com.termmed.reasoner.model.StatusMessage;

public class ServerStatus {

	public static enum STATUS {BUSY("Busy"),IDLE("Idle"), ERROR ("Error");
		private String name;

		STATUS(String name){
			this.name=name;
		}

		public String getName() {
			return name;
		}
		
	};
	static StatusMessage statusMessage=new StatusMessage(STATUS.BUSY.getName(),"Building myself");
	public static StatusMessage getStatusMessage(){
		return statusMessage;
	}
	public static void setStatusMessage(StatusMessage statusMessage) {
		ServerStatus.statusMessage = statusMessage;
	}
	public static void setStatusMessage(STATUS status, String processInfo) {
		
		ServerStatus.statusMessage = new StatusMessage(status.getName(),processInfo);
	}
	public static StatusMessage getStatusReset() {
		statusMessage= new StatusMessage(STATUS.IDLE.getName(),"Server waiting for request");
		return statusMessage;
	}
	public static boolean isIdle(){
		return statusMessage.isIdle();
	}
	public static boolean isBusy(){
		return statusMessage.isBusy();
	}
}
