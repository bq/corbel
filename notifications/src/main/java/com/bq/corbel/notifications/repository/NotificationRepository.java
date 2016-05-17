package com.bq.corbel.notifications.repository;

import com.bq.corbel.lib.mongo.repository.PartialUpdateRepository;
import com.bq.corbel.lib.queries.mongo.repository.GenericFindRepository;
import com.bq.corbel.notifications.model.NotificationTemplate;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Francisco Sanchez
 */
public interface NotificationRepository extends CrudRepository<NotificationTemplate, String>, GenericFindRepository<NotificationTemplate, String> {

    NotificationTemplate findByDomainAndName(String domain, String name);

    Long deleteByDomainAndName(String domain, String name);
}
