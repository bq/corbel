package com.bq.oss.corbel.iam.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

import com.bq.oss.corbel.iam.exception.GroupAlreadyExistsException;
import com.bq.oss.corbel.iam.exception.NoGroupException;
import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.corbel.iam.repository.GroupRepository;
import com.bq.oss.lib.ws.digest.Digester;

@RunWith(MockitoJUnitRunner.class) public class DefaultGroupServiceTest {

    private static final String ID = "id";
    private static final String NEW_ID = "newId";
    private static final String NAME = "name";
    private static final String DOMAIN = "domain";
    private static final Set<String> SCOPES = new HashSet<>(Arrays.asList("scope1", "scope2"));

    @Mock private GroupRepository groupRepository;
    @Mock private Digester digester;

    private GroupService groupService;

    @Before
    public void setUp() {
        groupService = new DefaultGroupService(groupRepository, digester);
    }

    @Test
    public void getAllGroupsTest() {
        when(groupRepository.findAll()).thenReturn(Collections.emptyList());

        List<Group> groups = groupService.getAll();

        assertThat(groups).isEqualTo(Collections.emptyList());

        verify(groupRepository).findAll();
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void getGroupTest() {
        Group expectedGroup = getGroup();

        when(groupRepository.findOne(ID)).thenReturn(expectedGroup);

        Optional<Group> group = groupService.get(ID);

        assertThat(group.isPresent()).isTrue();

        assertThat(group.get()).isEqualTo(expectedGroup);

        verify(groupRepository).findOne(ID);
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void getNullGroupTest() {
        Optional<Group> group = groupService.get(ID);

        assertThat(group.isPresent()).isFalse();

        verify(groupRepository).findOne(ID);
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void createGroupTest() throws GroupAlreadyExistsException {
        Group group = getGroup();

        when(digester.digest(eq("name_domain"))).thenReturn(NEW_ID);

        groupService.create(group);

        ArgumentCaptor<Group> capturedGroup = ArgumentCaptor.forClass(Group.class);

        verify(digester).digest(eq("name_domain"));
        verify(groupRepository).save(capturedGroup.capture());

        Group savedGroup = capturedGroup.getValue();

        assertThat(savedGroup.getId()).isEqualTo(NEW_ID);
        assertThat(savedGroup.getName()).isEqualTo(group.getName());
        assertThat(savedGroup.getDomain()).isEqualTo(group.getDomain());
        assertThat(savedGroup.getScopes()).isEqualTo(group.getScopes());

        verifyNoMoreInteractions(digester, groupRepository);
    }

    @Test(expected = GroupAlreadyExistsException.class)
    public void createAlreadyExistentGroupTest() throws GroupAlreadyExistsException {
        Group group = getGroup();

        when(digester.digest(eq("name_domain"))).thenReturn(NEW_ID);
        when(groupRepository.save(Mockito.<Group>any())).thenThrow(new DataIntegrityViolationException(NEW_ID));

        try {
            groupService.create(group);

        } catch (GroupAlreadyExistsException e) {

            ArgumentCaptor<Group> capturedGroup = ArgumentCaptor.forClass(Group.class);

            verify(digester).digest(eq("name_domain"));
            verify(groupRepository).save(capturedGroup.capture());

            Group savedGroup = capturedGroup.getValue();

            assertThat(savedGroup.getId()).isEqualTo(NEW_ID);
            assertThat(savedGroup.getName()).isEqualTo(group.getName());
            assertThat(savedGroup.getDomain()).isEqualTo(group.getDomain());
            assertThat(savedGroup.getScopes()).isEqualTo(group.getScopes());

            verifyNoMoreInteractions(digester, groupRepository);

            throw e;
        }
    }

    @Test
    public void deleteGroupTest() {
        groupService.delete(ID);

        verify(groupRepository).delete(ID);
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void updateGroupTest() throws NoGroupException {
        Group group = getGroup();

        when(groupRepository.patch(any())).thenReturn(true);

        groupService.update(NEW_ID, group);

        verify(groupRepository).patch(any());
        verifyNoMoreInteractions(groupRepository);
    }

    @Test(expected = NoGroupException.class)
    public void updateNonExistentGroupTest() throws NoGroupException {
        Group group = getGroup();

        when(groupRepository.patch(any())).thenReturn(false);

        try {
            groupService.update(NEW_ID, group);

        } catch (NoGroupException e) {

            verify(groupRepository).patch(any());
            verifyNoMoreInteractions(groupRepository);

            throw e;
        }
    }

    private Group getGroup() {
        return new Group(ID, NAME, DOMAIN, SCOPES);
    }
}
