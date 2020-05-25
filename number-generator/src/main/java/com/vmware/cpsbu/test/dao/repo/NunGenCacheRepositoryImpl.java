package com.vmware.cpsbu.test.dao.repo;

import java.util.UUID;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.vmware.cpsbu.test.constants.NumGenState;
import com.vmware.cpsbu.test.jpa.JpaTask;
import com.vmware.cpsbu.test.model.Task;

@Repository("nunGenCacheRepository")
public class NunGenCacheRepositoryImpl implements NunGenCacheRepository {

	private HashOperations<String, UUID, JpaTask> hashOperations;

	private static final String CACHE_KEY = "NumGen";

	public NunGenCacheRepositoryImpl(RedisTemplate<String, JpaTask> redisTemplate) {
		hashOperations = redisTemplate.opsForHash();
	}

	@Override
	public JpaTask persist(Task task) {
		JpaTask jpaTask = new JpaTask();
		jpaTask.setGoal(task.getGoal());
		jpaTask.setStep(task.getStep());
		jpaTask.setId(UUID.randomUUID());
		jpaTask.setStatus(NumGenState.IN_PROGRESS);
		hashOperations.putIfAbsent(CACHE_KEY, jpaTask.getId(), jpaTask);
		return jpaTask;
	}

	@Override
	public JpaTask update(JpaTask jpaTask) {
		hashOperations.put(CACHE_KEY, jpaTask.getId(), jpaTask);
		return jpaTask;
	}

	@Override
	public JpaTask findById(UUID id) {
		return (JpaTask) hashOperations.get(CACHE_KEY, id);
	}

	@Override
	public void delete(UUID id) {
		hashOperations.delete(CACHE_KEY, id);
	}
}
