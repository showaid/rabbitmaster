package com.wizes.rabbitmaster.entity;

import com.wizes.rabbitmaster.util.RoleType;

public class User {
	private String internalId;
	private String userId;
	private String name;
	private String email;
	private String locale;
	private String timeZone;
	private RoleType[] roles;
	private String[] groups;
	
	public String getInternalId() {
		return internalId;
	}
	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public String getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	public RoleType[] getRoles() {
		return roles;
	}
	public void setRoles(RoleType[] roles) {
		this.roles = roles;
	}
	public String[] getGroups() {
		return groups;
	}
	public void setGroups(String[] groups) {
		this.groups = groups;
	}
	
}
