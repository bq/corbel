package io.corbel.notifications.repository;

import io.corbel.lib.queries.mongo.repository.GenericFindRepository;
import io.corbel.notifications.model.NotificationConfigByDomain;
import org.springframework.data.repository.CrudRepository;

public interface NotificationConfigByDomainRepository extends CrudRepository<NotificationConfigByDomain, String>,
        GenericFindRepository<NotificationConfigByDomain, String> {

    NotificationConfigByDomain findByDomainAndTemplate(String domain, String template);


}
