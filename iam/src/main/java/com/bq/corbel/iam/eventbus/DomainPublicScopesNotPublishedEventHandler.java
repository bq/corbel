package com.bq.corbel.iam.eventbus;

import com.bq.corbel.event.DomainPublicScopesNotPublishedEvent;
import com.bq.corbel.eventbus.EventHandler;
import com.bq.corbel.iam.model.Domain;
import com.bq.corbel.iam.model.Scope;
import com.bq.corbel.iam.repository.DomainRepository;
import com.bq.corbel.iam.service.ScopeService;
import com.bq.corbel.lib.ws.auth.DefaultPublicAccessService;

import java.util.Collections;
import java.util.Set;

public class DomainPublicScopesNotPublishedEventHandler implements EventHandler<DomainPublicScopesNotPublishedEvent> {

    private final ScopeService scopeService;
    private final DomainRepository domainRepository;

    public DomainPublicScopesNotPublishedEventHandler(ScopeService scopeService, DomainRepository domainRepository) {
        this.scopeService = scopeService;
        this.domainRepository = domainRepository;
    }

    @Override
    public void handle(DomainPublicScopesNotPublishedEvent domainPublicScopesNotPublishedEvent) {
        Domain domain = domainRepository.findOne(domainPublicScopesNotPublishedEvent.getDomain());
        String key = domainPublicScopesNotPublishedEvent.getDomain() + DefaultPublicAccessService.PUBLIC_SCOPES_SUFFIX;

        Set<Scope> publicScopes = domain != null ? scopeService.getScopes(domain.getPublicScopes()) : Collections.<Scope>emptySet();
        scopeService.addAuthorizationRulesForPublicAccess(key, publicScopes);
    }

    @Override
    public Class<DomainPublicScopesNotPublishedEvent> getEventType() {
        return DomainPublicScopesNotPublishedEvent.class;
    }
}
