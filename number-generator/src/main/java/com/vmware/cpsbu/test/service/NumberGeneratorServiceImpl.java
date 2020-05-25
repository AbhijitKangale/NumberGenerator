package com.vmware.cpsbu.test.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.vmware.cpsbu.test.constants.NumGenState;
import com.vmware.cpsbu.test.dao.repo.NunGenCacheRepository;
import com.vmware.cpsbu.test.jpa.JpaTask;
import com.vmware.cpsbu.test.model.Task;

@Service("NumberGeneratorService")
public class NumberGeneratorServiceImpl implements NumberGeneratorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NumberGeneratorServiceImpl.class);

	private static final String FILE_PATH_HEAD = "C:\\\\tmp\\\\";

	private static final String FILE_PATH_TAIL = "_output.txt";

	@Autowired
	private NunGenCacheRepository cache;

	@Override
	@Async("asyncExecutor")
	public CompletableFuture<JpaTask> generateNumbers(Task task) throws Exception {

		long start = System.currentTimeMillis();

		LOGGER.info("Number Generating task is In Progress");

		// Persisting Number Generator data in cache
		JpaTask japTask = cache.persist(task);

		LOGGER.info("Persisted task wiht UUID : {}", japTask.getId());

		BufferedWriter out = null;

		try {

			StringBuffer sb = new StringBuffer();
			for (int i = Integer.parseInt(task.getGoal()); i >= 0; i = i - Integer.parseInt(task.getStep())) {
				sb.append(i).append(',');
				Thread.sleep(5000);
			}
			out = new BufferedWriter(new FileWriter(FILE_PATH_HEAD + japTask.getId() + FILE_PATH_TAIL));
			// Removing tailing ',' before writing to file
			out.write(sb.substring(0, sb.length() - 1).toString());

		} catch (Exception e) {

			japTask.setStatus(NumGenState.ERROR);
			cache.update(japTask);
			LOGGER.info("Error while writing the numbers to file : {}", e.getMessage());
			return CompletableFuture.completedFuture(japTask);

		} finally {
			cache.update(japTask);
			if (null != out)
				try {
					out.close();
				} catch (IOException e) {
					LOGGER.info("Error while closing the BufferedWriter : {}", e.getMessage());
				}
		}

		japTask.setStatus(NumGenState.SUCCESS);
		cache.update(japTask);

		LOGGER.info("Total time taken for generateNumbers : {}", (System.currentTimeMillis() - start));

		return CompletableFuture.completedFuture(japTask);
	}

	@Override
	@Async("asyncExecutor")
	public CompletableFuture<JpaTask> getTask(UUID uuid) throws Exception {
		JpaTask jpaTask = null;
		try {
			jpaTask = cache.findById(uuid);
			return CompletableFuture.completedFuture(jpaTask);
		} catch (Exception e) {
			LOGGER.info("Error while fetching the task : {}", e.getMessage());
			return CompletableFuture.completedFuture(jpaTask);
		}
	}

	@Override
	@Async("asyncExecutor")
	public CompletableFuture<String> getNumList(UUID uuid) throws Exception {

		File file = new File(FILE_PATH_HEAD + uuid + FILE_PATH_TAIL);

		LOGGER.info("Reading Numbers from File : {}", file.getPath());

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			return CompletableFuture.completedFuture(sb.toString());

		} catch (Exception e) {

			LOGGER.info("Error while reading numbers from file : {}", e.getMessage());
			return CompletableFuture.completedFuture(e.getMessage());

		} finally {

			if (null != br)
				try {
					br.close();
				} catch (IOException e) {
					LOGGER.info("Error while closing the BufferedReader : {}", e.getMessage());
				}
		}
	}
}
