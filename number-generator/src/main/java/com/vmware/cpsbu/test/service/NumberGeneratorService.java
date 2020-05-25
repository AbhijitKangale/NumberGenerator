package com.vmware.cpsbu.test.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.vmware.cpsbu.test.jpa.JpaTask;
import com.vmware.cpsbu.test.model.Task;

public interface NumberGeneratorService {

	public CompletableFuture<JpaTask> generateNumbers(Task task) throws Exception;

	public CompletableFuture<JpaTask> getTask(UUID UUID) throws Exception;

	public CompletableFuture<String> getNumList(UUID UUID) throws Exception;
}
