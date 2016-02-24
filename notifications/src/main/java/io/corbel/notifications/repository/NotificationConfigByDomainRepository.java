package io.corbel.notifications.repository;

import io.corbel.lib.queries.mongo.repository.GenericFindRepository;
import io.corbel.notifications.model.NotificationConfigByDomain;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NotificationConfigByDomainRepository extends CrudRepository<NotificationConfigByDomain, String>,
        GenericFindRepository<NotificationConfigByDomain, String> {

    NotificationConfigByDomain findByDomainAndTemplate(String domain, String template);

    List<NotificationConfigByDomain> findByDomain(String domain);


}
