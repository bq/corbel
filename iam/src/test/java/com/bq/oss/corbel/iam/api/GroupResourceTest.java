package com.bq.oss.corbel.iam.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.bq.oss.corbel.iam.exception.GroupAlreadyExistsException;
import com.bq.oss.corbel.iam.exception.NoGroupException;
import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.corbel.iam.service.GroupService;
import com.bq.oss.lib.queries.builder.QueryParametersBuilder;
import com.bq.oss.lib.queries.parser.*;
import com.bq.oss.lib.ws.api.error.GenericExceptionMapper;
import com.bq.oss.lib.ws.api.error.JsonValidationExceptionMapper;
import com.bq.oss.lib.ws.auth.*;
import com.bq.oss.lib.ws.queries.QueryParametersProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;

public class GroupResourceTest {

    private static final GroupService groupService = mock(GroupService.class);

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String DOMAIN = "domain";
    private static final Set<String> SCOPES = new HashSet<>(Arrays.asList("scope1", "scope2"));
    private static final String TOKEN = "token";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 20;

    public static final QueryParser queryParserMock = mock(QueryParser.class);
    public static final AggregationParser aggregationParserMock = mock(AggregationParser.class);
    public static final SortParser sortParserMock = mock(SortParser.class);
    public static final SearchParser searchParserMock = mock(SearchParser.class);
    public static final PaginationParser paginationParserMock = mock(PaginationParser.class);

    public static final QueryParametersBuilder queryParametersBuilder = new QueryParametersBuilder(queryParserMock, aggregationParserMock,
            sortParserMock, paginationParserMock, searchParserMock);

    private static final BearerTokenAuthenticator authenticatorMock = mock(BearerTokenAuthenticator.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);

    private static OAuthFactory oAuthFactory = new OAuthFactory<>(authenticatorMock, "realm", AuthorizationInfo.class);
    @SuppressWarnings("unchecked") private static CookieOAuthFactory<AuthorizationInfo> cookieOAuthFactory = mock(CookieOAuthFactory.class);
    @SuppressWarnings("unchecked") private static final AuthorizationRequestFilter filter = spy(
            new AuthorizationRequestFilter(oAuthFactory, cookieOAuthFactory, ""));

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder().addResource(new GroupResource(groupService))
            .addProvider(filter).addProvider(new AuthorizationInfoProvider().getBinder()).addProvider(GenericExceptionMapper.class)
            .addProvider(JsonValidationExceptionMapper.class)
            .addProvider(new QueryParametersProvider(DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE, queryParametersBuilder).getBinder()).build();

    @Before
    public void setUp() throws AuthenticationException {
        reset(authorizationInfoMock, authenticatorMock, cookieOAuthFactory);

        when(cookieOAuthFactory.provide()).thenReturn(authorizationInfoMock);
        when(authenticatorMock.authenticate(TOKEN)).thenReturn(com.google.common.base.Optional.of(authorizationInfoMock));
        when(authorizationInfoMock.getDomainId()).thenReturn(DOMAIN);

        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TOKEN);
        doReturn(requestMock).when(filter).getRequest();
        doNothing().when(filter).checkAccessRules(eq(authorizationInfoMock), any());
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
        when(groupService.getAll(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/group").request().get(List.class)).hasSize(0);
    }

    @Test
    public void getGroupTest() {
        Group group = new Group(ID, NAME, DOMAIN, SCOPES);

        when(groupService.get(eq(ID), eq(DOMAIN))).thenReturn(Optional.of(group));

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/group/" + ID).request().get(Group.class)).isEqualTo(group);
    }

    @Test
    public void getNonexistentGroupTest() {
        when(groupService.get(eq(ID), eq(DOMAIN))).thenReturn(Optional.empty());

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/group/" + ID).request().get(Response.class).getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void deleteGroupTest() {
        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/group/" + ID).request().delete().getStatus())
                .isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void updateGroupTest() throws JsonProcessingException {
        Group group = new Group(null, NAME, null, null);

        String groupJson = new ObjectMapper().writer().writeValueAsString(group);

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/group/" + ID).request().put(Entity.json(groupJson), Response.class)
                .getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void updateNonexistentGroupTest() throws GroupAlreadyExistsException, NoGroupException, JsonProcessingException {
        updateGroupWithExceptionTest(new NoGroupException(ID), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void updateAlreadyExistentGroupTest() throws GroupAlreadyExistsException, NoGroupException, JsonProcessingException {
        updateGroupWithExceptionTest(new GroupAlreadyExistsException(ID), Response.Status.CONFLICT.getStatusCode());
    }

    private void updateGroupWithExceptionTest(Exception exception, int status)
            throws JsonProcessingException, NoGroupException, GroupAlreadyExistsException {
        Group group = new Group(ID, NAME, DOMAIN, null);

        doThrow(exception).when(groupService).update(eq(group));

        String groupJson = new ObjectMapper().writer().writeValueAsString(group);

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/group/" + ID).request().put(Entity.json(groupJson), Response.class)
                .getStatus()).isEqualTo(status);
    }
}
