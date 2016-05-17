package com.bq.corbel.iam.service;

import com.bq.corbel.iam.exception.UserProfileConfigurationException;
import com.bq.corbel.iam.model.Domain;
import com.bq.corbel.iam.model.User;
import com.bq.corbel.iam.model.UserToken;
import com.bq.corbel.iam.repository.CreateUserException;
import com.bq.corbel.lib.queries.request.Pagination;
import com.bq.corbel.lib.queries.request.ResourceQuery;
import com.bq.corbel.lib.queries.request.Sort;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Rubén Carrasco
 * 
 */
public interface UserService {

    String findUserDomain(String id);

    User update(User user);

    User create(User user) throws CreateUserException;

    User findById(String id);

    List<User> findUsersByDomain(String domain, ResourceQuery resourceQuery, Pagination pagination, Sort sort);

    void signOut(String userId, Optional<String> accessToken);

    default void signOut(String userId) {
        signOut(userId, Optional.empty());
    }

    void delete(User user);

    boolean existsByUsernameAndDomain(String username, String domainId);

    boolean existsByEmailAndDomain(String email, String domainId);

    void invalidateAllTokens(String userId);

    UserToken getSession(String token);

    User getUserProfile(User user, Set<String> userProfileFields) throws UserProfileConfigurationException;

    void sendMailResetPassword(String email, String clientId, String domain);

    List<User> findUserProfilesByDomain(Domain domain, ResourceQuery resourceQuery, Pagination pagination, Sort sort)
            throws UserProfileConfigurationException;

    User findByDomainAndUsername(String domain, String username);

    User findByDomainAndEmail(String domain, String email);

    void addScopes(String userId, String... scopes);

    void removeScopes(String userId, String... scopes);

    long countUsersByDomain(String domainId, ResourceQuery query);
}
