package com.bq.oss.corbel.iam.repository.decorator;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.repository.UserRepository;
import com.bq.oss.lib.queries.request.AggregationResult;
import com.bq.oss.lib.queries.request.Pagination;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bq.oss.lib.queries.request.Sort;

/**
 * @author Francisco Sanchez
 */
public class UserRepositoryDecorator implements UserRepository {

    protected UserRepository decoratedUserRepository;

    public UserRepositoryDecorator(UserRepository decoratedUserRepository) {
        this.decoratedUserRepository = decoratedUserRepository;

    }

    @Override
    public User findById(String id) {
        return decoratedUserRepository.findById(id);
    }

    @Override
    public User findByUsernameAndDomain(String username, String domainId) {
        return decoratedUserRepository.findByUsernameAndDomain(username, domainId);
    }

    @Override
    public User findByDomainAndEmail(String domain, String email) {
        return decoratedUserRepository.findByDomainAndEmail(domain, email);
    }

    @Override
    public List<User> find(ResourceQuery resourceQuery, Pagination pagination, Sort sort) {
        return decoratedUserRepository.find(resourceQuery, pagination, sort);
    }

    @Override
    public AggregationResult count(ResourceQuery resourceQuery) {
        return decoratedUserRepository.count(resourceQuery);
    }

    @Override
    public User save(User user) {
        return decoratedUserRepository.save(user);
    }

    @Override
    public <S extends User> List<S> save(Iterable<S> ses) {
        return decoratedUserRepository.save(ses);
    }

    @Override
    public List<User> findAll() {
        return decoratedUserRepository.findAll();
    }

    @Override
    public List<User> findAll(org.springframework.data.domain.Sort sort) {
        return decoratedUserRepository.findAll(sort);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return decoratedUserRepository.findAll(pageable);
    }

    @Override
    public User findOne(String s) {
        return decoratedUserRepository.findOne(s);
    }

    @Override
    public boolean exists(String s) {
        return decoratedUserRepository.exists(s);
    }

    @Override
    public Iterable<User> findAll(Iterable<String> strings) {
        return decoratedUserRepository.findAll(strings);
    }

    @Override
    public long count() {
        return decoratedUserRepository.count();
    }

    @Override
    public void delete(String s) {
        decoratedUserRepository.delete(s);
    }

    @Override
    public void delete(User entity) {
        decoratedUserRepository.delete(entity);
    }

    @Override
    public void delete(Iterable<? extends User> entities) {
        decoratedUserRepository.delete(entities);
    }

    @Override
    public void deleteAll() {
        decoratedUserRepository.deleteAll();
    }

    @Override
    public void addScopes(String s, String... scopes) {
        decoratedUserRepository.addScopes(s, scopes);

    }

    @Override
    public void removeScopes(String s, String... scopes) {
        decoratedUserRepository.removeScopes(s, scopes);
    }

    @Override
    public String findUserDomain(String id) {
        return decoratedUserRepository.findUserDomain(id);
    }

    @Override
    public boolean existsByUsernameAndDomain(String username, String domainId) {
        return decoratedUserRepository.existsByUsernameAndDomain(username, domainId);
    }

    @Override
    public void deleteByDomain(String domainId) {
        decoratedUserRepository.deleteByDomain(domainId);
    }

}
