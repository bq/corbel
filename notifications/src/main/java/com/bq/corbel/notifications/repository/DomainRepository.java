package com.bq.corbel.notifications.repository;

import com.bq.corbel.lib.queries.mongo.repository.GenericFindRepository;
import com.bq.corbel.notifications.model.Domain;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DomainRepository extends CrudRepository<Domain, String>,
        GenericFindRepository<Domain, String> {

}
