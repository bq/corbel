package com.bq.oss.corbel.iam.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.bq.oss.corbel.iam.exception.GroupAlreadyExistsException;
import com.bq.oss.corbel.iam.exception.NoGroupException;
import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.corbel.iam.service.GroupService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.ResourceTestRule;

public class GroupResourceTest {

    private static final GroupService groupService = mock(GroupService.class);
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String DOMAIN = "domain";
    private static final Set<String> SCOPES = new HashSet<>(Arrays.asList("scope1", "scope2"));

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder().addResource(new GroupResource(groupService)).build();

    @Before
    public void setUp() {
        reset(groupService);
    }

    @Test
    public void createGroupTest() throws JsonProcessingException, GroupAlreadyExistsException {
        Group group = new Group(null, NAME, DOMAIN, SCOPES);
        Group createdGroup = new Group(ID, NAME, DOMAIN, SCOPES);

        when(groupService.create(eq(group))).thenReturn(createdGroup);

        String groupJson = new ObjectMapper().writer().writeValueAsString(group);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/group").request().post(Entity.json(groupJson),
                Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(((String) response.getHeaders().getFirst("Location")).endsWith(createdGroup.getId())).isTrue();
    }

    @Test
    public void createAlreadyExistingGroupTest() throws GroupAlreadyExistsException, JsonProcessingException {
        Group group = new Group(null, NAME, DOMAIN, SCOPES);

        when(groupService.create(eq(group))).thenThrow(new GroupAlreadyExistsException(NAME + " " + ID));

        String groupJson = new ObjectMapper().writer().writeValueAsString(group);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/group").request().post(Entity.json(groupJson),
                Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getAllGroupsTest() {
        when(groupService.getAll()).thenReturn(Collections.emptyList());

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/group").request().get(List.class)).hasSize(0);
    }

    @Test
    public void getGroupTest() {
        Group group = new Group(ID, NAME, DOMAIN, SCOPES);

        when(groupService.get(ID)).thenReturn(Optional.of(group));

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/group/" + ID).request().get(Group.class)).isEqualTo(group);
    }

    @Test
    public void getNonexistentGroupTest() {
        when(groupService.get(ID)).thenReturn(Optional.empty());

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/group/" + ID).request().get(Response.class).getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void deleteGroupTest() {
        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/group/" + ID).request().delete().getStatus())
                .isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void updateGroup() throws JsonProcessingException {
        Group group = new Group(null, NAME, null, null);

        String groupJson = new ObjectMapper().writer().writeValueAsString(group);

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/group/" + ID).request().put(Entity.json(groupJson), Response.class)
                .getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void updateNonexistentGroup() throws JsonProcessingException, NoGroupException {
        Group group = new Group(null, NAME, null, null);

        doThrow(new NoGroupException(ID)).when(groupService).update(eq(ID), eq(group));

        String groupJson = new ObjectMapper().writer().writeValueAsString(group);

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/group/" + ID).request().put(Entity.json(groupJson), Response.class)
                .getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
