package com.bq.corbel.iam.repository;

import com.bq.corbel.iam.model.User;
import com.bq.corbel.lib.queries.mongo.repository.GenericFindRepository;

/**
 * @author Alberto J. Rubio
 */
public interface UserRepository extends GenericFindRepository<User, String>, HasScopesRepository<String>, UserRepositoryCustom {

    User findById(String id);

    User findByUsernameAndDomain(String username, String domainId);

    User findByDomainAndEmail(String domain, String email);

}
