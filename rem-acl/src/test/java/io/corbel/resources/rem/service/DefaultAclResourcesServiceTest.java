package io.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.AclPermission;
import io.corbel.resources.rem.model.ManagedCollection;
import io.corbel.resources.rem.request.ResourceId;

/**
 * @author Rubén Carrasco
 */
@RunWith(MockitoJUnitRunner.class) @SuppressWarnings("unchecked") public class DefaultAclResourcesServiceTest {

    private static final String ALL = "ALL";
    private static final ResourceId ID_NOT_ALLOWED = new ResourceId("idNotAllowed");
    private static final ResourceId RESOURCE_ID = new ResourceId("idAllowed");
    private static final String USER_ID = "userId";
    private static final Optional<String> OPT_USER_ID = Optional.of(USER_ID);
    private static final String GROUP_ID = "groupId";
    private static final Collection<String> GROUPS = Collections.singletonList(GROUP_ID);
    private static final String DOMAIN_ID = "domainId";
    private static final String TYPE = "type";
    private static final String ADMINS_COLLECTION = "adminsCollection";
    private static final char JOINER = ':';
    private static final String MANAGED_COLLECTION_ID = DOMAIN_ID + JOINER + TYPE;
    private static final ResourceId MANAGED_COLLECTION_RESOURCE = new ResourceId(MANAGED_COLLECTION_ID);
    private static final ResourceId MANAGED_DOMAIN_RESOURCE = new ResourceId(DOMAIN_ID);

    @Mock private RemService remService;
    @Mock private Rem resmiRem;
    private JsonParser parser = new JsonParser();
    private Gson gson = new Gson();

    private DefaultAclResourcesService aclService = new DefaultAclResourcesService(gson, ADMINS_COLLECTION);

    @Before
    public void setUp() throws Exception {
        when(remService.getRem(DefaultAclResourcesService.RESMI_GET)).thenReturn(resmiRem);
        aclService.setRemService(remService);
    }

    @Test
    public void testAllowedWithUserId() {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.USER_PREFIX + USER_ID);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(OPT_USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.READ)).isTrue();
    }

    @Test
    public void testAllowedWithAll() {
        Response response = mockResponseWithAcl(ALL);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(OPT_USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.READ)).isTrue();
    }

    @Test
    public void testAllowedWithoutAclObject() {
        Response response = mockResponse();
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(OPT_USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.READ)).isTrue();
    }

    @Test
    public void testNotAllowedOperationWithUserId() {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.USER_PREFIX + USER_ID);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(OPT_USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.WRITE)).isFalse();
    }

    @Test
    public void testNotAllowedOperationWithAll() {
        Response response = mockResponseWithAcl(ALL);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(OPT_USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.WRITE)).isFalse();
    }

    @Test
    public void testNotAllowedWithUserId() {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.USER_PREFIX + "asdf");
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(OPT_USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.WRITE)).isFalse();
    }

    @Test
    public void testNotAllowed() {
        Response response = mockResponseWithEmptyAcl();
        when(resmiRem.resource(any(), eq(ID_NOT_ALLOWED), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(OPT_USER_ID, GROUPS, TYPE, ID_NOT_ALLOWED, AclPermission.READ)).isFalse();
    }

    @Test
    public void testAllowedWithGroupId() {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.GROUP_PREFIX + GROUP_ID);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(OPT_USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.READ)).isTrue();
    }

    @Test
    public void testNotAllowedWithGroupId() {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.GROUP_PREFIX + GROUP_ID);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(OPT_USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.WRITE)).isFalse();
    }

    @Test
    public void testNotAllowedWithBadAcl() {
        Response response = mockResponseWithBadAcl(DefaultAclResourcesService.USER_PREFIX + USER_ID);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(OPT_USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.READ)).isFalse();
    }

    private Response mockResponseWithEmptyAcl() {
        return mockResponse("{ \"_acl\": {} }");
    }

    private Response mockResponseWithAcl(String scope) {
        return mockResponse(
                "{ \"_acl\": { \"" + scope + "\": { \"permission\": \"READ\", \"properties\": {\"email\": \"asdf@funkifake.com\"} } } }");
    }

    private Response mockResponseWithBadAcl(String scope) {
        return mockResponse("{ \"_acl\": { \"" + scope + "\": { \"permission\": {}, \"properties\": {} } } }");
    }

    private Response mockResponse() {
        return mockResponse("{}");
    }

    private Response mockResponse(String json) {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.getEntity()).thenReturn(parser.parse(json));
        return response;
    }

    @Test(expected = WebApplicationException.class)
    public void testFailedManagedCollection() {
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
        when(responseMock.getStatusInfo()).thenReturn(mock(Response.StatusType.class));

        when(resmiRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any())).thenReturn(responseMock);

        try {
            assertThat(aclService.isManagedBy(DOMAIN_ID, OPT_USER_ID, GROUPS, TYPE)).isTrue();
        } catch (WebApplicationException e) {
            verify(remService).getRem(DefaultAclResourcesService.RESMI_GET);
            verify(resmiRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any());
            verifyNoMoreInteractions(remService, resmiRem);
            throw e;
        }
    }

    @Test
    public void testManagedCollection() {
        ManagedCollection managedCollection = new ManagedCollection(MANAGED_COLLECTION_ID, Collections.singletonList(USER_ID),
                Collections.emptyList());
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(responseMock.getEntity()).thenReturn(gson.toJsonTree(managedCollection));

        when(resmiRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any())).thenReturn(responseMock);

        assertThat(aclService.isManagedBy(DOMAIN_ID, OPT_USER_ID, GROUPS, TYPE)).isTrue();

        verify(remService).getRem(DefaultAclResourcesService.RESMI_GET);
        verify(resmiRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any());
        verifyNoMoreInteractions(remService, resmiRem);
    }

    @Test
    public void testManagedCollectionByGroup() throws IOException {
        ManagedCollection managedCollection = new ManagedCollection(MANAGED_COLLECTION_ID, Collections.emptyList(),
                Collections.singletonList(GROUP_ID));
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(responseMock.getEntity()).thenReturn(gson.toJsonTree(managedCollection));

        when(resmiRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any())).thenReturn(responseMock);

        assertThat(aclService.isManagedBy(DOMAIN_ID, OPT_USER_ID, GROUPS, TYPE)).isTrue();

        verify(remService).getRem(DefaultAclResourcesService.RESMI_GET);
        verify(resmiRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any());
        verifyNoMoreInteractions(remService, resmiRem);
    }

    @Test
    public void testManagedCollectionNotByUser() {
        ManagedCollection managedCollection = new ManagedCollection(MANAGED_COLLECTION_ID, Collections.emptyList(),
                Collections.emptyList());
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(responseMock.getEntity()).thenReturn(gson.toJsonTree(managedCollection));

        Response domainResponseMock = mock(Response.class);
        when(domainResponseMock.getStatus()).thenReturn(Response.Status.NOT_FOUND.getStatusCode());
        when(domainResponseMock.getStatusInfo()).thenReturn(mock(Response.StatusType.class));

        when(resmiRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any())).thenReturn(responseMock);
        when(resmiRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_DOMAIN_RESOURCE), any(), any())).thenReturn(domainResponseMock);

        assertThat(aclService.isManagedBy(DOMAIN_ID, OPT_USER_ID, GROUPS, TYPE)).isFalse();

        verify(remService).getRem(DefaultAclResourcesService.RESMI_GET);
        verify(resmiRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any());
        verify(resmiRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_DOMAIN_RESOURCE), any(), any());
        verifyNoMoreInteractions(remService, resmiRem);
    }

    @Test
    public void testManagedCollectionByDomainAdmin() {
        ManagedCollection managedCollection = new ManagedCollection(MANAGED_COLLECTION_ID, Collections.emptyList(),
                Collections.emptyList());
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(responseMock.getEntity()).thenReturn(gson.toJsonTree(managedCollection));

        ManagedCollection domainManagedCollection = new ManagedCollection(DOMAIN_ID, Collections.singletonList(USER_ID),
                Collections.emptyList());
        Response domainResponseMock = mock(Response.class);
        when(domainResponseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(domainResponseMock.getEntity()).thenReturn(gson.toJsonTree(domainManagedCollection));

        when(resmiRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any())).thenReturn(responseMock);
        when(resmiRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_DOMAIN_RESOURCE), any(), any())).thenReturn(domainResponseMock);

        assertThat(aclService.isManagedBy(DOMAIN_ID, OPT_USER_ID, GROUPS, TYPE)).isTrue();

        verify(remService).getRem(DefaultAclResourcesService.RESMI_GET);
        verify(resmiRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any());
        verify(resmiRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_DOMAIN_RESOURCE), any(), any());
        verifyNoMoreInteractions(remService, resmiRem);
    }

    @Test
    public void testManagedCollectionByGroupDomainAdmin() {
        ManagedCollection managedCollection = new ManagedCollection(MANAGED_COLLECTION_ID, Collections.emptyList(),
                Collections.emptyList());
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(responseMock.getEntity()).thenReturn(gson.toJsonTree(managedCollection));

        ManagedCollection domainManagedCollection = new ManagedCollection(DOMAIN_ID, Collections.emptyList(),
                Collections.singletonList(GROUP_ID));
        Response domainResponseMock = mock(Response.class);
        when(domainResponseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(domainResponseMock.getEntity()).thenReturn(gson.toJsonTree(domainManagedCollection));

        when(resmiRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any())).thenReturn(responseMock);
        when(resmiRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_DOMAIN_RESOURCE), any(), any())).thenReturn(domainResponseMock);

        assertThat(aclService.isManagedBy(DOMAIN_ID, OPT_USER_ID, GROUPS, TYPE)).isTrue();

        verify(remService).getRem(DefaultAclResourcesService.RESMI_GET);
        verify(resmiRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any());
        verify(resmiRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_DOMAIN_RESOURCE), any(), any());
        verifyNoMoreInteractions(remService, resmiRem);
    }

}
