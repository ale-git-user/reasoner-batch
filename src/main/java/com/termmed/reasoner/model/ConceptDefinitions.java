package com.termmed.reasoner.model;

public class ConceptDefinitions {

	String definitions;
	Integer limit;
	Boolean previousResults;

	public Boolean getPreviousResults() {
		return previousResults;
	}

	public void setPreviousResults(Boolean previousResults) {
		this.previousResults = previousResults;
	}

	public String getDefinitions() {
		return definitions;
	}

	public void setDefinitions(String definitions) {
		this.definitions = definitions;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}
}
