package com.vmware.cpsbu.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.cpsbu.test.config.NumGeneratorApp;
import com.vmware.cpsbu.test.constants.NumGenState;
import com.vmware.cpsbu.test.controller.NumberGeneratorControllerImpl;
import com.vmware.cpsbu.test.dao.repo.NunGenCacheRepository;
import com.vmware.cpsbu.test.model.Response;
import com.vmware.cpsbu.test.model.Task;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = NumGeneratorApp.class)
public class NumGeneratorAppTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(NumGeneratorAppTest.class);

	private static final String FILE_PATH_HEAD = "C:\\\\tmp\\\\";

	private static final String FILE_PATH_TAIL = "_output.txt";

	@Mock
	NumberGeneratorControllerImpl controller;

	private MockMvc mvc;

	@Autowired
	WebApplicationContext context;

	@Autowired
	private NunGenCacheRepository cache;

	List<String> testUuidList = null;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
		testUuidList = new ArrayList<>();
	}

	@Test
	public void testCreate() throws Exception {

		Task task = createTaskModel();
		Response response = createTask(task);
		assertNotNull(response.getTask());
		testUuidList.add(response.getTask());
	}

	@Test
	public void testRetrieveStatus() throws Exception {

		Task task = createTaskModel();
		Response response = createTask(task);
		testUuidList.add(response.getTask());
		MvcResult resultStatus = mvc
				.perform(get("/api/tasks/" + response.getTask() + "/status").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		Response statusResponse = (Response) resultStatus.getAsyncResult();

		assertEquals(NumGenState.SUCCESS.toString(), statusResponse.getResult());
	}

	@Test
	public void testRetrieveTask() throws Exception {

		Task task = createTaskModel();
		Response response = createTask(task);
		testUuidList.add(response.getTask());
		MvcResult resultTask = mvc.perform(
				get("/api/tasks/" + response.getTask() + "?action=get_numlist").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		Response taskResponse = (Response) resultTask.getAsyncResult();

		assertEquals("4,2,0", taskResponse.getResult());
	}

	@Test
	public void testCreate_EmptyOrNullGoal() throws Exception {

		try {
			Task task = createTaskModelWithEmptyOrNullGoal();
			createTask(task);
		} catch (Exception e) {
			assertTrue(e.getCause().toString().contains("Goal is mandatory or it cannot be empty"));
		}
	}

	@Test
	public void testCreate_GoalLessThanZero() throws Exception {

		try {
			Task task = createTaskModelWithGoalLessThanZero();
			createTask(task);
		} catch (Exception e) {
			assertTrue(e.getCause().toString().contains("Goal cannot be less than zero"));
		}
	}

	@Test
	public void testCreate_GoalAsNonNumber() throws Exception {

		try {
			Task task = createTaskModelWithGoalAsNonNumber();
			createTask(task);
		} catch (Exception e) {
			assertTrue(e.getCause().toString().contains("Goal should be a number"));
		}
	}

	@Test
	public void testCreate_StepGreaterThanGoal() throws Exception {

		try {
			Task task = createTaskModelWithStepGreaterThanGoal();
			createTask(task);
		} catch (Exception e) {
			assertTrue(e.getCause().toString().contains("Step cannot be greater than goal"));
		}
	}

	private Task createTaskModel() {
		Task task = new Task();
		task.setGoal("4");
		task.setStep("2");
		return task;
	}

	private Task createTaskModelWithEmptyOrNullGoal() {
		Task task = new Task();
		task.setGoal("");
		task.setStep("2");
		return task;
	}

	private Task createTaskModelWithGoalLessThanZero() {
		Task task = new Task();
		task.setGoal("-10");
		task.setStep("2");
		return task;
	}

	private Task createTaskModelWithGoalAsNonNumber() {
		Task task = new Task();
		task.setGoal("aa4");
		task.setStep("2");
		return task;
	}

	private Task createTaskModelWithStepGreaterThanGoal() {
		Task task = new Task();
		task.setGoal("2");
		task.setStep("10");
		return task;
	}

	private String objectToJson(Object obj) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(obj);
	}

	private Response createTask(Task task) throws Exception {
		String taskJson = objectToJson(task);
		MvcResult result = mvc.perform(post("/api/generate").content(taskJson).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();

		// Setting this timeout for testing purpose
		result.getRequest().getAsyncContext().setTimeout(20000);

		return (Response) result.getAsyncResult();
	}

	@After
	public void destroy() {
		// deleting the files and it's state from memory after testing
		try {
			for (String uuid : testUuidList) {
				cache.delete(UUID.fromString(uuid));
				LOGGER.info("Deleting task wiht it's file for UUID : {}", uuid);
				new File(FILE_PATH_HEAD + uuid + FILE_PATH_TAIL).delete();
			}
		} catch (Exception e) {
			LOGGER.info("Error while deleting the file and removing it's state from memory : {}", e.getMessage());
		}
	}
}
