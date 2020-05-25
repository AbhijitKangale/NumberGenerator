package com.vmware.cpsbu.test.dao.repo;

import java.util.UUID;

import com.vmware.cpsbu.test.jpa.JpaTask;
import com.vmware.cpsbu.test.model.Task;

public interface NunGenCacheRepository {

	JpaTask persist(Task task);

	JpaTask update(JpaTask task);

	JpaTask findById(UUID id);

	void delete(UUID id);
}
