package com.vmware.cpsbu.test.controller;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.vmware.cpsbu.test.model.Response;
import com.vmware.cpsbu.test.model.Task;

public interface NumberGeneratorController {

	public CompletableFuture<Response> create(Task task) throws Exception;

	public CompletableFuture<Response> retrieveStatus(UUID id) throws Exception;

	public CompletableFuture<Response> retrieveTask(UUID id, String action) throws Exception;
}
