package com.termmed.reasoner.model;

import com.termmed.ServerStatus;

public class StatusMessage {
	String status;
	String processInfo;
	public StatusMessage(String status, String processInfo) {
		super();
		this.status = status;
		this.processInfo = processInfo;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getProcessInfo() {
		return processInfo;
	}
	public void setProcessInfo(String processInfo) {
		this.processInfo = processInfo;
	}
	public boolean isIdle() {
		return status==ServerStatus.STATUS.IDLE.getName();
	}
	public boolean isBusy() {
		return status==ServerStatus.STATUS.BUSY.getName();
	}
	
}
