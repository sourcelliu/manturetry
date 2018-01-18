package com.mantuliu.retry.core.entity;

import java.io.Serializable;

public class RetryParameter implements Serializable {

	private String classname;
	private String value;
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
