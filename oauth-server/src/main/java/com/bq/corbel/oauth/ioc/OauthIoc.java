package com.bq.corbel.oauth.ioc;

import com.bq.corbel.eventbus.ioc.EventBusIoc;
import com.bq.corbel.eventbus.service.EventBus;
import com.bq.corbel.lib.config.ConfigurationIoC;
import com.bq.corbel.lib.token.factory.TokenFactory;
import com.bq.corbel.lib.token.ioc.OneTimeAccessTokenIoc;
import com.bq.corbel.lib.token.ioc.TokenIoc;
import com.bq.corbel.lib.token.parser.TokenParser;
import com.bq.corbel.lib.token.provider.SessionProvider;
import com.bq.corbel.lib.token.reader.TokenReader;
import com.bq.corbel.lib.token.repository.OneTimeAccessTokenRepository;
import com.bq.corbel.lib.ws.auth.BasicAuthProvider;
import com.bq.corbel.lib.ws.auth.JsonUnauthorizedHandler;
import com.bq.corbel.lib.ws.auth.OAuthProvider;
import com.bq.corbel.lib.ws.cors.ioc.CorsIoc;
import com.bq.corbel.lib.ws.dw.ioc.CommonFiltersIoc;
import com.bq.corbel.lib.ws.dw.ioc.DropwizardIoc;
import com.bq.corbel.oauth.api.*;
import com.bq.corbel.oauth.api.auth.ClientCredentialsAuthenticator;
import com.bq.corbel.oauth.api.auth.TokenAuthenticator;
import com.bq.corbel.oauth.cli.dsl.OauthShell;
import com.bq.corbel.oauth.filter.AuthFilterRegistrar;
import com.bq.corbel.oauth.filter.FilterRegistry;
import com.bq.corbel.oauth.filter.InMemoryFilterRegistry;
import com.bq.corbel.oauth.mail.EmailValidationConfiguration;
import com.bq.corbel.oauth.mail.NotificationConfiguration;
import com.bq.corbel.oauth.model.Client;
import com.bq.corbel.oauth.repository.ClientRepository;
import com.bq.corbel.oauth.repository.UserRepository;
import com.bq.corbel.oauth.repository.decorator.LowerCaseDecorator;
import com.bq.corbel.oauth.service.ClientService;
import com.bq.corbel.oauth.service.DefaultClientService;
import com.bq.corbel.oauth.service.DefaultMailChangePasswordService;
import com.bq.corbel.oauth.service.DefaultMailResetPasswordService;
import com.bq.corbel.oauth.service.DefaultMailValidationService;
import com.bq.corbel.oauth.service.DefaultSendNotificationService;
import com.bq.corbel.oauth.service.DefaultUserService;
import com.bq.corbel.oauth.service.MailChangePasswordService;
import com.bq.corbel.oauth.service.MailResetPasswordService;
import com.bq.corbel.oauth.service.MailValidationService;
import com.bq.corbel.oauth.service.SendNotificationService;
import com.bq.corbel.oauth.service.UserService;
import com.bq.corbel.oauth.session.DefaultSessionBuilder;
import com.bq.corbel.oauth.session.DefaultSessionCookieFactory;
import com.bq.corbel.oauth.session.SessionBuilder;
import com.bq.corbel.oauth.session.SessionCookieFactory;
import com.bq.corbel.oauth.token.TokenExpireTime;
import io.dropwizard.auth.UnauthorizedHandler;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.auth.oauth.OAuthFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.google.gson.Gson;

/**
 * @author by Alberto J. Rubio
 */
@Configuration @Import({ConfigurationIoC.class, OauthMongoIoc.class, TokenVerifiersIoc.class, OneTimeAccessTokenIoc.class, TokenIoc.class,
        CommonFiltersIoc.class, DropwizardIoc.class, CorsIoc.class, EventBusIoc.class}) @ComponentScan({"com.bq.corbel.oauth.filter.plugin",
        "com.bqreaders.silkroad.oauth.filter.plugin"}) public class OauthIoc {

    @Autowired private Environment env;

    @Autowired private EventBus eventBus;

    @Autowired private UserRepository userRepository;

    @Autowired private ClientRepository clientRepository;

    @Autowired private OneTimeAccessTokenRepository oneTimeAccessTokenRepository;

    private UserRepository getUserRepository() {
        return new LowerCaseDecorator(userRepository);
    }

    @Bean
    public UserService getUserService(MailValidationService mailValidationService, MailResetPasswordService mailResetPasswordService,
            MailChangePasswordService mailChangePasswordService) {
        return new DefaultUserService(getUserRepository(), mailValidationService, mailResetPasswordService, mailChangePasswordService);
    }

    @Bean
    public ClientService getClientService() {
        return new DefaultClientService(clientRepository);
    }

    @Bean
    public Gson getGson() {
        return new Gson();
    }

    @Bean
    public TokenResource getAccessTokenResource(TokenParser tokenParser, TokenFactory tokenFactory) {
        return new TokenResource(tokenParser, tokenFactory, getClientService(), getUserRepository(), getExpireTime());
    }

    @Bean
    public SessionCookieFactory getSessionCookieFactory() {
        return new DefaultSessionCookieFactory(env.getProperty("session.cookie.path"), env.getProperty("session.cookie.domain"),
                env.getProperty("session.cookie.comment"), env.getProperty("session.cookie.maxAge", Integer.class), env.getProperty(
                "session.cookie.secure", Boolean.class));
    }

    @Bean
    public AuthorizeResource getAuthorizeResource(TokenFactory tokenFactory, UserService userService, ClientService clientService,
            SessionBuilder sessionBuilder) {
        return new AuthorizeResource(userService, tokenFactory, clientService, getSessionCookieFactory(), getExpireTime(), sessionBuilder,
                getFilterRegistry());
    }

    @Bean
    public SessionBuilder getSessionBuilder(TokenFactory tokenFactory) {
        return new DefaultSessionBuilder(tokenFactory, Integer.valueOf(env.getProperty("session.cookie.maxAge")));
    }

    public TokenExpireTime getExpireTime() {
        return new TokenExpireTime(env.getProperty("oauth.token.codeDurationInSec", Long.class), env.getProperty(
                "oauth.token.accessTokenDurationInSec", Long.class));
    }

    @Bean
    public UserResource getUserResource(UserService userService, ClientService clientService) {
        return new UserResource(userService, clientService);
    }

    @Bean
    public UsernameResource getUsernameResource(UserService userService) {
        return new UsernameResource(userService);
    }

    @Bean
    public SignoutResource getSignoutResource(SessionCookieFactory sessionCookieFactory) {
        return new SignoutResource(sessionCookieFactory);
    }

    @Bean
    public MailValidationService getMailValidationService(TokenFactory tokenFactory, ClientService clientService) {
        return new DefaultMailValidationService(mailValidationConfiguration(), getSendMailService(), tokenFactory, clientService);
    }

    @Bean
    public EmailValidationConfiguration mailValidationConfiguration() {
        return new EmailValidationConfiguration(env.getProperty("email.validation.notification"),
                env.getProperty("email.validation.clientUrl"),
                env.getProperty("oauth.token.emailValidationTokenDurationInSec", Long.class), env.getProperty("email.validation.enabled",
                        Boolean.class));
    }

    @Bean
    public MailResetPasswordService getMailResetPasswordService(TokenFactory tokenFactory, SendNotificationService sendNotificationService,
            ClientService clientService) {
        return new DefaultMailResetPasswordService(getMailResetPasswordConfiguration(), sendNotificationService, tokenFactory);
    }

    @Bean
    public MailChangePasswordService getMailChangePasswordService() {
        return new DefaultMailChangePasswordService(env.getProperty("user.changePassword.notification"), getSendMailService());
    }

    private NotificationConfiguration getMailResetPasswordConfiguration() {
        return new NotificationConfiguration(env.getProperty("email.resetPassword.notification"),
                env.getProperty("email.resetPassword.clientUrl"),
                env.getProperty("oauth.token.resetPasswordTokenDurationInSec", Long.class));
    }

    @Bean
    public SendNotificationService getSendMailService() {
        return new DefaultSendNotificationService(eventBus);
    }

    @Bean(name = "sessionProvider")
    public SessionProvider getSessionProvider(TokenParser tokenParser) {
        return new SessionProvider(tokenParser);
    }

    @Bean
    public UnauthorizedHandler getUnauthorizedHandler() {
        return new JsonUnauthorizedHandler();
    }

    @Bean
    public BasicAuthProvider getBasicAuthProvider() {
        BasicAuthFactory<Client> factory = new BasicAuthFactory<>(new ClientCredentialsAuthenticator(clientRepository), "access token",
                Client.class);
        factory.responseBuilder(getUnauthorizedHandler());
        return new BasicAuthProvider(factory);
    }

    @Bean
    public OAuthProvider getOAuthProvider(TokenParser tokenParser) {
        OAuthFactory<TokenReader> factory = new OAuthFactory<>(new TokenAuthenticator(tokenParser), "access token", TokenReader.class);
        factory.responseBuilder(getUnauthorizedHandler());
        return new OAuthProvider(factory);
    }

    @Bean
    public OauthShell getOauthShell() {
        return new OauthShell(clientRepository, getUserRepository());
    }

    @Bean
    public FilterRegistry getFilterRegistry() {
        return new InMemoryFilterRegistry();
    }

    @Bean
    AuthFilterRegistrar getAuthFilterRegistrar() {
        return new AuthFilterRegistrar(getFilterRegistry());
    }

    protected OneTimeAccessTokenRepository getOneTimeAccessTokenRepository() {
        return oneTimeAccessTokenRepository;
    }

}
