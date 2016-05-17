package com.bq.corbel.oauth.repository;

import com.bq.corbel.lib.mongo.repository.PartialUpdateRepository;
import com.bq.corbel.oauth.model.User;

/**
 * @author Alberto J. Rubio
 */
public interface UserRepository extends PartialUpdateRepository<User, String>, UserRepositoryCustom {

    User findByUsername(String username);

    User findByEmail(String email);

    User findById(String id);

    User findByEmailAndDomain(String email, String domain);

    User findByUsernameAndDomain(String username, String domain);

}
