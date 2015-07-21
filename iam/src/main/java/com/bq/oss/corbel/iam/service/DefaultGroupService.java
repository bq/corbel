package com.bq.oss.corbel.iam.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;

import com.bq.oss.corbel.iam.exception.GroupAlreadyExistsException;
import com.bq.oss.corbel.iam.exception.NoGroupException;
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
    public Optional<Group> get(String id, String domain) {
        return Optional.ofNullable(groupRepository.findByIdAndDomain(id, domain));
    }

    @Override
    public List<Group> getAll(String domain, List<ResourceQuery> resourceQueries, Pagination pagination, Sort sort) {
        return groupRepository.find(resourceQueries, pagination, sort).stream().filter(group -> group.getDomain().equals(domain))
                .collect(Collectors.toList());
    }

    @Override
    public Group create(Group group) throws GroupAlreadyExistsException {
        Group groupToSave = new Group(group);

        groupToSave.setId(null);

        try {
            return groupRepository.save(groupToSave);
        } catch (DataIntegrityViolationException e) {
            throw new GroupAlreadyExistsException(group.getName() + " in domain " + group.getDomain());
        }
    }

    @Override
    public Group update(Group group) throws NoGroupException, GroupAlreadyExistsException {
        if (group.getName() == null) {
            if (!groupRepository.patch(group)) {
                throw new NoGroupException(group.getId());
            }
            return group;
        }

        Group groupToUpdate = get(group.getId(), group.getDomain()).orElseThrow(() -> new NoGroupException(group.getId()));
        groupToUpdate.setName(group.getName());
        Group newGroup = create(groupToUpdate);

        delete(group.getId(), group.getDomain());

        Group groupUpdate = new Group(group);
        groupUpdate.setName(null);
        groupUpdate.setId(newGroup.getId());
        return update(groupUpdate);
    }

    @Override
    public void delete(String id, String domain) {
        groupRepository.deleteByIdAndDomain(id, domain);
    }

}
