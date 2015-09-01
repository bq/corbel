package io.corbel.iam.service;

import io.corbel.iam.exception.GroupAlreadyExistsException;
import io.corbel.iam.exception.NotExistentScopeException;
import io.corbel.iam.model.Group;
import io.corbel.iam.repository.GroupRepository;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultGroupService implements GroupService {

    private final GroupRepository groupRepository;
    private final ScopeService scopeService;

    public DefaultGroupService(GroupRepository groupRepository, ScopeService scopeService) {
        this.groupRepository = groupRepository;
        this.scopeService = scopeService;
    }

    @Override
    public Optional<Group> get(String id) {
        return Optional.ofNullable(groupRepository.findOne(id));
    }

    @Override
    public Optional<Group> get(String id, String domain) {
        return Optional.ofNullable(groupRepository.findByIdAndDomain(id, domain));
    }

    @Override
    public List<Group> getAll(String domain, List<ResourceQuery> resourceQueries, Pagination pagination, Sort sort) {
        return groupRepository.findByDomain(domain, resourceQueries, pagination, sort);
    }

    @Override
    public Group create(Group group) throws GroupAlreadyExistsException, NotExistentScopeException {
        group.setId(null);
        try {
            checkScopes(group.getScopes());
            groupRepository.insert(group);
            return group;
        } catch (DataIntegrityViolationException e) {
            throw new GroupAlreadyExistsException(group.getName() + " in domain " + group.getDomain());
        }
    }

    @Override
    public void addScopes(String id, String... scopes) throws NotExistentScopeException {
        checkScopes(Arrays.asList(scopes));
        groupRepository.addScopes(id, scopes);
    }

    @Override
    public void removeScopes(String id, String... scopes) {
        groupRepository.removeScopes(id, scopes);
    }

    @Override
    public void delete(String id, String domain) {
        groupRepository.deleteByIdAndDomain(id, domain);
    }

    private void checkScopes(Collection<String> scopes) throws NotExistentScopeException {
        String notExistentScopes = scopes.stream().filter(scope -> scopeService.getScope(scope)==null).collect(Collectors.joining(", "));
        if (!notExistentScopes.isEmpty()) {
            throw new NotExistentScopeException(notExistentScopes);
        }
    }

}
