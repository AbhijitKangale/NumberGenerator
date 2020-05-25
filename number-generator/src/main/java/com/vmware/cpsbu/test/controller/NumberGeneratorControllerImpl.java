package com.vmware.cpsbu.test.controller;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.cpsbu.test.constants.ResponseType;
import com.vmware.cpsbu.test.jpa.JpaTask;
import com.vmware.cpsbu.test.model.Response;
import com.vmware.cpsbu.test.model.Task;
import com.vmware.cpsbu.test.service.NumberGeneratorService;

@RestController
@RequestMapping("/api")
public class NumberGeneratorControllerImpl implements NumberGeneratorController {

	private static final String NO_TASK_ERROR_MESSAGE = "No task exists for given UUID";

	@Autowired
	NumberGeneratorService numberGeneratorService;

	public NumberGeneratorControllerImpl() {
		super();
	}

	@RequestMapping(value = "/generate", method = { RequestMethod.POST })
	@ResponseStatus(HttpStatus.CREATED)
	@Override
	public CompletableFuture<Response> create(@RequestBody Task task) throws Exception {

		validateTask(task);

		return numberGeneratorService.generateNumbers(task)
				.thenApplyAsync(jpaTask -> generateResponse(jpaTask, ResponseType.CREATE, null, null))
				.exceptionally(ex -> generateResponse(null, ResponseType.CREATE, null, ex.getMessage()));
	}

	@RequestMapping(value = "/tasks/{UUID}/status", method = { RequestMethod.GET })
	@ResponseStatus(HttpStatus.OK)
	@Override
	public CompletableFuture<Response> retrieveStatus(@PathVariable(name = "UUID", required = true) UUID uuid)
			throws Exception {

		CompletableFuture<Response> response = numberGeneratorService.getTask(uuid)
				.thenApplyAsync(jpaTask -> generateResponse(jpaTask, ResponseType.STATUS, null, null))
				.exceptionally(ex -> generateResponse(null, ResponseType.STATUS, null, ex.getMessage()));

		if (null != response.get() && response.get().getError() == null && response.get().getResult() == null
				&& response.get().getTask() == null)
			return CompletableFuture
					.completedFuture(generateResponse(null, ResponseType.STATUS, null, NO_TASK_ERROR_MESSAGE));

		return response;
	}

	@RequestMapping(value = "/tasks/{UUID}", method = { RequestMethod.GET })
	@ResponseStatus(HttpStatus.OK)
	@Override
	public CompletableFuture<Response> retrieveTask(@PathVariable(name = "UUID", required = true) UUID uuid,
			@RequestParam(value = "action", required = true) String action) throws Exception {

		if (!action.equals("get_numlist"))
			throw new ValidationException("Invalid action specified");

		CompletableFuture<JpaTask> jpaTask = numberGeneratorService.getTask(uuid);

		if (null == jpaTask.get())
			return CompletableFuture
					.completedFuture(generateResponse(null, ResponseType.GET, null, NO_TASK_ERROR_MESSAGE));

		return numberGeneratorService.getNumList(uuid)
				.thenApplyAsync(list -> generateResponse(null, ResponseType.GET, list, null))
				.exceptionally(ex -> generateResponse(null, ResponseType.GET, null, ex.getMessage()));

	}

	private Response generateResponse(JpaTask jpaTask, ResponseType type, String numList, String errorMessage) {

		Response response = new Response();

		if (null != jpaTask) {
			if (type.equals(ResponseType.STATUS)) {
				if (null != jpaTask.getStatus())
					response.setResult(jpaTask.getStatus().toString());
			}

			if (type.equals(ResponseType.CREATE)) {
				if (null != jpaTask.getId())
					response.setTask(jpaTask.getId().toString());
			}
		}

		if (type.equals(ResponseType.GET)) {
			if (null != numList)
				response.setResult(numList);
		}

		if (null != errorMessage)
			response.setError(errorMessage);

		return response;
	}

	private void validateTask(Task task) {
		if (null == task.getGoal() || task.getGoal().trim().isEmpty())
			throw new ValidationException("Goal is mandatory or it cannot be empty");

		if (null != task.getGoal() && task.getGoal().trim().charAt(0) == '-')
			throw new ValidationException("Goal cannot be less than zero");

		if (!task.getGoal().trim().chars().allMatch(Character::isDigit))
			throw new ValidationException("Goal should be a number");

		if (null == task.getStep() || task.getStep().trim().isEmpty())
			throw new ValidationException("Step is mandatory or it cannot be empty");

		if (null != task.getStep() && task.getStep().trim().charAt(0) == '-')
			throw new ValidationException("Step cannot be less than zero");

		if (!task.getStep().trim().chars().allMatch(Character::isDigit))
			throw new ValidationException("Step should be a number");

		if (Integer.parseInt(task.getStep()) > Integer.parseInt(task.getGoal()))
			throw new ValidationException("Step cannot be greater than goal");
	}
}
