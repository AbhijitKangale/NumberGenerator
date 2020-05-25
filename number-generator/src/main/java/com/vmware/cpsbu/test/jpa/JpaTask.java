package com.vmware.cpsbu.test.jpa;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.vmware.cpsbu.test.constants.NumGenState;

@Entity
public class JpaTask implements Serializable {

	private static final long serialVersionUID = -5123429921568282219L;

	@Id
	private UUID id;

	private String goal;

	private String step;

	private NumGenState status;

	public JpaTask() {

	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getGoal() {
		return goal;
	}

	public void setGoal(String goal) {
		this.goal = goal;
	}

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public NumGenState getStatus() {
		return status;
	}

	public void setStatus(NumGenState status) {
		this.status = status;
	}
}
