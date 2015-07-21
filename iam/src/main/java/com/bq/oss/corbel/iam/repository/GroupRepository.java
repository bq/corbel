package com.bq.oss.corbel.iam.repository;

import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.lib.mongo.repository.PartialUpdateRepository;

public interface GroupRepository extends PartialUpdateRepository<Group, String> {}
