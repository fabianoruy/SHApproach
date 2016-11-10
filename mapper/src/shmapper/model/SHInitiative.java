package shmapper.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Represents a Standard Harmonization Initiative. */
public class SHInitiative {
    private String domain;
    private String purpose;
    private String scope;
    private String description;
    private List<Package> packages;
    private List<Match> matches;
    private Map<String, Notion> notionMap;

    public SHInitiative(String domain, String purpose, String scope) {
	this.domain = domain;
	this.purpose = purpose;
	this.scope = scope;
	this.packages = new ArrayList<Package>();
	this.matches = new ArrayList<Match>();
	this.notionMap = new HashMap<String, Notion>();
    }

    public String getDomain() {
	return domain;
    }

    public String getPurpose() {
	return purpose;
    }

    public String getScope() {
	return scope;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public List<Package> getAllPackages() {
	return this.packages;
    }

    public void addPackage(Package pack) {
	this.packages.add(pack);
    }

    public void addMatch(Match match) {
	this.matches.add(match);
    }

    public List<Notion> getAllNotions() {
	return new ArrayList<Notion>(notionMap.values());
    }

    public void addNotion(Notion notion) {
	this.notionMap.put(notion.getAstahClass().getId(), notion);
    }

    public Notion getNotionById(String id) {
	return notionMap.get(id);
    }

    /* Returns a Package by id. */
    public Package getPackage(String id) {
	for (Package pack : packages) {
	    if (pack.getId().equals(id)) {
		return pack;
	    }
	}
	return null;
    }

    /* Returns the SEON View. */
    public SeonView getSeonView() {
	for (Package pack : packages) {
	    if (pack instanceof SeonView) {
		return (SeonView) pack;
	    }
	}
	return null;
    }

    @Override
    public String toString() {
	return this.domain;
    }

}