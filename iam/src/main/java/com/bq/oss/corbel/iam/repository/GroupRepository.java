package com.bq.oss.corbel.iam.repository;

import java.util.List;

import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.lib.mongo.repository.PartialUpdateRepository;
import com.bq.oss.lib.queries.mongo.repository.GenericFindRepository;

public interface GroupRepository
        extends PartialUpdateRepository<Group, String>, GenericFindRepository<Group, String>, GroupRepositoryCustom {

    Group findByIdAndDomain(String id, String domain);

    List<Group> findByDomain(String domain);

}
