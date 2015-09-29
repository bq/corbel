package io.corbel.oauth.api;

import com.google.common.base.Optional;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.ws.auth.*;
import io.corbel.oauth.model.User;
import io.corbel.oauth.service.UserService;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.joda.time.Instant;
import org.junit.ClassRule;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Ricardo Martínez
 */
public class UsernameResourceTest extends UserResourceTestBase{

    private static final String URL_PREFIX = "/" + ApiVersion.CURRENT + "/username/";

    private static final UserService userServiceMock = mock(UserService.class);
    private static final BearerTokenAuthenticator authenticatorMock = mock(BearerTokenAuthenticator.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
    private static final TokenReader tokenReaderMock = mock(TokenReader.class);
    private static final TokenInfo tokenMock = mock(TokenInfo.class);

    private static OAuthFactory oAuthFactory = new OAuthFactory<>(authenticatorMock, "realm", AuthorizationInfo.class);
    private static final AuthorizationRequestFilter filter = spy(new AuthorizationRequestFilter(oAuthFactory, null, "", false));


    @ClassRule
    public static ResourceTestRule RULE = ResourceTestRule.builder().addResource(new UsernameResource(userServiceMock))
            .addProvider(filter).addProvider(new AuthorizationInfoProvider().getBinder()).build();

    public UsernameResourceTest() throws Exception {
        when(tokenMock.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(authorizationInfoMock.getTokenReader()).thenReturn(tokenReaderMock);
        when(tokenReaderMock.getInfo()).thenReturn(tokenMock);
        when(tokenMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);
        when(authenticatorMock.authenticate(TEST_GOOD_TOKEN)).thenReturn(Optional.of(authorizationInfoMock));
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);

        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_GOOD_TOKEN);
        doReturn(requestMock).when(filter).getRequest();
        doNothing().when(filter).checkAccessRules(eq(authorizationInfoMock), any(), any());
    }

    @Test
    public void testExistUser() {
        User user = createTestUser();
        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(user);
        when(userServiceMock.existsByUsernameAndDomain(USERNAME_TEST, TEST_DOMAIN_ID)).thenReturn(true);
        Response response = RULE.client().target(URL_PREFIX + USERNAME_TEST).request().header(AUTHORIZATION, "Bearer " + TEST_GOOD_TOKEN)
                .head();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testNotExistUser() {
        when(userServiceMock.existsByUsernameAndDomain(USERNAME_TEST, TEST_DOMAIN_ID)).thenReturn(false);
        Response response = RULE.client().target(URL_PREFIX + USERNAME_TEST).request().header(AUTHORIZATION, "Bearer " + TEST_GOOD_TOKEN)
                .head();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testGetUserIdByUsernameOK() {
        User user = createTestUser();
        when(userServiceMock.findByUserNameAndDomain(USERNAME_TEST, TEST_DOMAIN_ID)).thenReturn(user);
        User response = RULE.client().target(URL_PREFIX + USERNAME_TEST).request().header(AUTHORIZATION, "Bearer " + TEST_GOOD_TOKEN)
                .get(User.class);
        verify(userServiceMock, times(1)).findByUserNameAndDomain(USERNAME_TEST, TEST_DOMAIN_ID);

        assertEquals(response.getId(), TEST_USER_ID);
    }

    @Test
    public void testGetUserIdByUsernameKO() {
        User user = createTestUser();
        when(userServiceMock.findByUserNameAndDomain(USERNAME_TEST, TEST_DOMAIN_ID)).thenReturn(user);
        Response response = RULE.client().target(URL_PREFIX + USERNAME_TEST + Instant.now().toString()).request().header(AUTHORIZATION, "Bearer " + TEST_GOOD_TOKEN)
                .get();

        assertThat(response.getStatus()).isEqualTo(404);
    }
}
