package com.bq.oss.corbel.iam.service;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;

import com.bq.oss.corbel.iam.exception.GroupAlreadyExistsException;
import com.bq.oss.corbel.iam.exception.NoGroupException;
import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.corbel.iam.repository.GroupRepository;
import com.bq.oss.lib.ws.digest.Digester;

public class DefaultGroupService implements GroupService {

    private final GroupRepository groupRepository;
    private final Digester digester;

    public DefaultGroupService(GroupRepository groupRepository, Digester digester) {
        this.groupRepository = groupRepository;
        this.digester = digester;
    }

    @Override
    public Optional<Group> get(String id) {
        return Optional.ofNullable(groupRepository.findOne(id));
    }

    @Override
    public List<Group> getAll() {
        return groupRepository.findAll();
    }

    @Override
    public Group create(Group group) throws GroupAlreadyExistsException {
        Group groupToSave = new Group(group);

        groupToSave.setId(digester.digest(group.getName() + "_" + group.getDomain()));

        try {
            return groupRepository.save(groupToSave);
        } catch (DataIntegrityViolationException e) {
            throw new GroupAlreadyExistsException(group.getName() + " in domain " + group.getDomain());
        }
    }

    @Override
    public void update(String id, Group group) throws NoGroupException {
        Group groupToUpdate = new Group(group);
        groupToUpdate.setId(id);

        if (!groupRepository.patch(groupToUpdate)) {
            throw new NoGroupException(id);
        }
    }

    @Override
    public void delete(String id) {
        groupRepository.delete(id);
    }

}
