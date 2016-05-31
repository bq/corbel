package com.bq.corbel.iam.repository;

import com.bq.corbel.iam.model.Domain;

/**
 * @author Cristian del Cerro
 */
public interface DomainRepositoryCustom {

    void addDefaultScopes(String id, String... scopes);

    void removeDefaultScopes(String id, String... scopes);

    void addPublicScopes(String id, String... scopes);

    void removePublicScopes(String id, String... scopes);

    void insertDomain(Domain domain);
}
