package com.termmed.reasoner.model;

import java.util.ArrayList;
import java.util.List;

public class Concept {

	String conceptId;
	String defaultTerm;
	ConceptList subClasses;
	ConceptList directSubClasses;
	ConceptList superClasses;
	ConceptList directSuperClasses;
	ConceptList equivalentClasses;

	public Concept getPreviousVersion() {
		return previousVersion;
	}

	public void setPreviousVersion(Concept previousVersion) {
		this.previousVersion = previousVersion;
	}

	Concept previousVersion;
	public String getConceptId() {
		return conceptId;
	}
	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}
	public String getDefaultTerm() {
		return defaultTerm;
	}
	public void setDefaultTerm(String defaultTerm) {
		this.defaultTerm = defaultTerm;
	}
	public Concept(String conceptId, String defaultTerm) {
		super();
		this.conceptId = conceptId;
		this.defaultTerm = defaultTerm;
	}
	public ConceptList getSubClasses() {
		return subClasses;
	}
	public void setSubClasses(ConceptList subClasses) {
		this.subClasses = subClasses;
	}
	public ConceptList getDirectSubClasses() {
		return directSubClasses;
	}
	public void setDirectSubClasses(ConceptList directSubClasses) {
		this.directSubClasses = directSubClasses;
	}
	public ConceptList getSuperClasses() {
		return superClasses;
	}
	public void setSuperClasses(ConceptList superClasses) {
		this.superClasses = superClasses;
	}
	public ConceptList getDirectSuperClasses() {
		return directSuperClasses;
	}
	public void setDirectSuperClasses(ConceptList directSuperClasses) {
		this.directSuperClasses = directSuperClasses;
	}
	public ConceptList getEquivalentClasses() {
		return equivalentClasses;
	}
	public void setEquivalentClasses(ConceptList equivalentClasses) {
		this.equivalentClasses = equivalentClasses;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Concept){

			return (this.conceptId.equals(((Concept) obj).getConceptId()));
		}
		return super.equals(obj);
	}
}
