package com.bq.oss.corbel.iam.service;

import java.util.List;
import java.util.Optional;

import com.bq.oss.corbel.iam.exception.GroupAlreadyExistsException;
import com.bq.oss.corbel.iam.exception.NoGroupException;
import com.bq.oss.corbel.iam.model.Group;

public interface GroupService {
    Optional<Group> get(String id);

    List<Group> getAll();

    Group create(Group group) throws GroupAlreadyExistsException;

    void update(String id, Group group) throws NoGroupException;

    void delete(String id);
}
