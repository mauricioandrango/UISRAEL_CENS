package com.cens.backend.censbackend.dto.user;

import java.io.Serializable;

public class LoginRequestDTO implements Serializable {

	private static final long serialVersionUID = -2929554742075366976L;

	private String username;

	private String password;

	
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
