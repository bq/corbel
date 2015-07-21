package com.bq.oss.corbel.iam.service;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;

import com.bq.oss.corbel.iam.exception.GroupAlreadyExistsException;
import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.corbel.iam.repository.GroupRepository;
import com.bq.oss.lib.queries.request.Pagination;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bq.oss.lib.queries.request.Sort;

public class DefaultGroupService implements GroupService {

    private final GroupRepository groupRepository;

    public DefaultGroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
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
    public Group create(Group group) throws GroupAlreadyExistsException {
        group.setId(null);

        try {
            return groupRepository.save(group);
        } catch (DataIntegrityViolationException e) {
            throw new GroupAlreadyExistsException(group.getName() + " in domain " + group.getDomain());
        }
    }

    @Override
    public void addScopes(String id, List<String> scopes) {
        groupRepository.addScopes(id, listToArray(scopes));
    }

    @Override
    public void removeScopes(String id, List<String> scopes) {
        groupRepository.removeScopes(id, listToArray(scopes));
    }

    private String[] listToArray(List<String> list) {
        String array[] = new String[list.size()];

        int i = 0;

        for (String e : list) {
            array[i++] = e;
        }

        return array;
    }

    @Override
    public void delete(String id, String domain) {
        groupRepository.deleteByIdAndDomain(id, domain);
    }

}
