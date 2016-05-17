package com.bq.corbel.iam.repository;

import com.bq.corbel.iam.model.Group;
import com.bq.corbel.lib.queries.request.Pagination;
import com.bq.corbel.lib.queries.request.ResourceQuery;
import com.bq.corbel.lib.queries.request.Sort;

import java.util.List;

public interface GroupRepositoryCustom {

    List<Group> findByDomain(String domain, List<ResourceQuery> resourceQueries, Pagination pagination, Sort sort);

    void deleteScopes(String... scopesId);

    void insert(Group group);
}
