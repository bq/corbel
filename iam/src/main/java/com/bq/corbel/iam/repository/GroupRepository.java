package com.bq.corbel.iam.repository;

import com.bq.corbel.iam.model.Group;

import com.bq.corbel.lib.mongo.repository.PartialUpdateRepository;
import com.bq.corbel.lib.queries.mongo.repository.GenericFindRepository;

public interface GroupRepository extends PartialUpdateRepository<Group, String>, GenericFindRepository<Group, String>,
        HasScopesRepository<String>, GroupRepositoryCustom {

    Group findByIdAndDomain(String id, String domain);

    Group findByNameAndDomain(String name, String domain);

    Long deleteByIdAndDomain(String id, String domain);

    void deleteScopes(String... scopesId);

}
