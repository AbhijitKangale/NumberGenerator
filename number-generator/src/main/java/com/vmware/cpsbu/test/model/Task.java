package com.vmware.cpsbu.test.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Task implements Serializable {

	private static final long serialVersionUID = -3489256832111879832L;

	private String goal;

	private String step;

	public Task() {
	}

	@JsonProperty("Goal")
	public String getGoal() {
		return goal;
	}

	public void setGoal(String goal) {
		this.goal = goal;
	}

	@JsonProperty("Step")
	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}
}
