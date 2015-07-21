package com.bq.oss.corbel.iam.repository;

import java.util.List;

import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.lib.queries.request.Pagination;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bq.oss.lib.queries.request.Sort;

public interface GroupRepositoryCustom {

    List<Group> findByDomain(String domain, List<ResourceQuery> resourceQueries, Pagination pagination, Sort sort);

}
