package com.bq.corbel.iam.repository;

import com.bq.corbel.iam.model.Domain;
import com.bq.corbel.lib.mongo.repository.PartialUpdateRepository;
import com.bq.corbel.lib.queries.mongo.repository.GenericFindRepository;

/**
 * @author Alberto J. Rubio
 */
public interface DomainRepository extends PartialUpdateRepository<Domain, String>, GenericFindRepository<Domain, String>,
        DomainRepositoryCustom, HasScopesRepository<String> {

    String FIELD_DEFAULT_SCOPES = "defaultScopes";

    String FIELD_PUBLIC_SCOPES = "publicScopes";

}
