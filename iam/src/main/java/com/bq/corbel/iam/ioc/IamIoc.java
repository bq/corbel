package com.bq.corbel.iam.ioc;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

import net.oauth.jsontoken.Checker;
import net.oauth.jsontoken.JsonTokenParser;
import net.oauth.jsontoken.crypto.SignatureAlgorithm;
import net.oauth.jsontoken.discovery.VerifierProvider;
import net.oauth.jsontoken.discovery.VerifierProviders;
import net.oauth.signatures.SignedJsonAssertionAudienceChecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import com.bq.corbel.event.DeviceEvent;
import com.bq.corbel.event.DomainDeletedEvent;
import com.bq.corbel.event.DomainPublicScopesNotPublishedEvent;
import com.bq.corbel.event.ScopeUpdateEvent;
import com.bq.corbel.eventbus.EventHandler;
import com.bq.corbel.eventbus.ioc.EventBusListeningIoc;
import com.bq.corbel.eventbus.service.EventBus;
import com.bq.corbel.iam.api.*;
import com.bq.corbel.iam.auth.AuthorizationRequestContextFactory;
import com.bq.corbel.iam.auth.AuthorizationRule;
import com.bq.corbel.iam.auth.provider.*;
import com.bq.corbel.iam.auth.rule.*;
import com.bq.corbel.iam.cli.dsl.IamShell;
import com.bq.corbel.iam.eventbus.DeviceDeletedEventHandler;
import com.bq.corbel.iam.eventbus.DomainDeletedEventHandler;
import com.bq.corbel.iam.eventbus.DomainPublicScopesNotPublishedEventHandler;
import com.bq.corbel.iam.eventbus.ScopeModifiedEventHandler;
import com.bq.corbel.iam.jwt.ClientVerifierProvider;
import com.bq.corbel.iam.jwt.TokenUpgradeVerifierProvider;
import com.bq.corbel.iam.model.*;
import com.bq.corbel.iam.repository.*;
import com.bq.corbel.iam.repository.decorator.LowerCaseDecorator;
import com.bq.corbel.iam.scope.MustacheScopeFillStrategy;
import com.bq.corbel.iam.scope.ScopeFillStrategy;
import com.bq.corbel.iam.service.*;
import com.bq.corbel.iam.utils.DefaultTokenCookieFactory;
import com.bq.corbel.iam.utils.TokenCookieFactory;
import com.bq.corbel.lib.config.ConfigurationIoC;
import com.bq.corbel.lib.mongo.IdGenerator;
import com.bq.corbel.lib.mongo.IdGeneratorMongoEventListener;
import com.bq.corbel.lib.queries.parser.CustomJsonParser;
import com.bq.corbel.lib.queries.parser.JacksonQueryParser;
import com.bq.corbel.lib.queries.parser.QueryParser;
import com.bq.corbel.lib.queries.request.AggregationResultsFactory;
import com.bq.corbel.lib.queries.request.JsonAggregationResultsFactory;
import com.bq.corbel.lib.token.factory.TokenFactory;
import com.bq.corbel.lib.token.ioc.OneTimeAccessTokenIoc;
import com.bq.corbel.lib.token.parser.TokenParser;
import com.bq.corbel.lib.token.repository.OneTimeAccessTokenRepository;
import com.bq.corbel.lib.ws.auth.ioc.AuthorizationIoc;
import com.bq.corbel.lib.ws.auth.repository.AuthorizationRulesRepository;
import com.bq.corbel.lib.ws.cors.ioc.CorsIoc;
import com.bq.corbel.lib.ws.digest.DigesterFactory;
import com.bq.corbel.lib.ws.dw.ioc.CommonFiltersIoc;
import com.bq.corbel.lib.ws.dw.ioc.DropwizardIoc;
import com.bq.corbel.lib.ws.ioc.QueriesIoc;

/**
 * @author Alexander De Leon
 */
@SuppressWarnings("unused")
@Configuration
@Import({ConfigurationIoC.class, IamMongoIoc.class, IamProviderIoc.class,
        TokenVerifiersIoc.class, OneTimeAccessTokenIoc.class, DropwizardIoc.class, AuthorizationIoc.class, CorsIoc.class, QueriesIoc.class,
        EventBusListeningIoc.class, CommonFiltersIoc.class})
public class IamIoc {

    @Autowired(required = true)
    private Environment env;

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private DomainRepository domainRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ScopeRepository scopeRepository;
    @Autowired
    private AuthorizationRulesRepository authorizationRulesRepository;
    @Autowired
    private IdentityRepository identityRepository;
    @Autowired
    private OneTimeAccessTokenRepository oneTimeAccessTokenRepository;
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private GroupRepository groupRepository;

    private UserRepository getUserRepository() {
        return new LowerCaseDecorator(userRepository);
    }

    @Bean
    public EventHandler<DomainDeletedEvent> domainDeletedEventHandler() {
        return new DomainDeletedEventHandler(clientRepository, userRepository);
    }

    @Bean
    public EventHandler<DeviceEvent> getDeviceDeletedEventHandler() {
        return new DeviceDeletedEventHandler(authorizationRulesRepository, userTokenRepository);
    }

    @Bean
    public EventHandler<ScopeUpdateEvent> scopeUpdateEventHandler(CacheManager cacheManager, GroupRepository groupRepository) {
        return new ScopeModifiedEventHandler(cacheManager, groupRepository);
    }

    @Bean
    public EventHandler<DomainPublicScopesNotPublishedEvent> domainPublicScopesNotPublishedEventHandler(ScopeService scopeService, DomainRepository domainRepository) {
        return new DomainPublicScopesNotPublishedEventHandler(scopeService, domainRepository);
    }

    @Bean
    public AuthorizationService getAuthorizationService(RefreshTokenService refreshTokenService, TokenFactory tokenFactory,
            ScopeService scopeService, ScopesAuthorizationRule scopesAuthorizationRule, UserService userService,
            EventsService eventsService, DeviceService deviceService) {
        return new DefaultAuthorizationService(getJsonTokenParser(), getAuthorizationRules(scopesAuthorizationRule), tokenFactory,
                getAuthorizationRequestContextFactory(scopeService), scopeService, getAuthorizationProviderFactory(), refreshTokenService,
                userTokenRepository, userService, eventsService, deviceService);
    }

    @Bean
    public RefreshTokenService getRefreshTokenService(TokenParser tokenParser, TokenFactory tokenFactory,
                                                      OneTimeAccessTokenRepository oneTimeAccessTokenRepository) {
        return new DefaultRefreshTokenService(tokenParser, getUserRepository(), tokenFactory, env.getProperty(
                "iam.auth.refreshToken.maxExpirationInSeconds", Long.class), oneTimeAccessTokenRepository);
    }

    @Bean
    public JsonTokenParser getJsonTokenParser() {
        return new JsonTokenParser(getVerifierProviders(), getAudienceChecker());
    }

    protected OneTimeAccessTokenRepository getOneTimeAccessTokenRepository() {
        return oneTimeAccessTokenRepository;
    }

    @Bean
    public TokenResource getTokenResource(AuthorizationService authorizationService, UpgradeTokenService upgradeTokenService) {
        return new TokenResource(authorizationService, upgradeTokenService, getSessionCookieFactory());
    }

    @Bean
    public UserResource getUserResource(UserService userService, DomainService domainService, IdentityService identityService,
                                        DeviceService deviceService, AggregationResultsFactory aggregationResultsFactory) {
        return new UserResource(userService, domainService, identityService, deviceService, aggregationResultsFactory, Clock.systemUTC());
    }

    @Bean
    public UsernameResource getUsernameResource(UserService userService) {
        return new UsernameResource(userService);
    }

    @Bean
    public EmailResource getEmailResource(UserService userService) {
        return new EmailResource(userService);
    }

    @Bean
    public ScopeResource getScopeResource(ScopeService scopeService) {
        return new ScopeResource(scopeService);
    }

    @Bean
    public DomainResource getDomainResource(DomainService domainService) {
        return new DomainResource(domainService);
    }

    @Bean
    public ClientResource getClientResource(ClientService clientService, DomainService domainService) {
        return new ClientResource(clientService, domainService);
    }

    @Bean
    public GroupResource getGroupResource(GroupService groupService) {
        return new GroupResource(groupService);
    }

    @Bean
    public ClientService getClientService(ClientRepository clientRepository, AggregationResultsFactory aggregationResultsFactory) {
        return new DefaultClientService(clientRepository, aggregationResultsFactory);
    }

    @Bean
    public IdentityService getIdentityService() {
        return new DefaultIdentityService(identityRepository);
    }

    @Bean
    DeviceService getDeviceService(DeviceRepository deviceRepository, EventsService eventsService) {
        return new DefaultDeviceService(deviceRepository, getDeviceIdGenerator(), eventsService, Clock.systemUTC());
    }

    @Bean
    public UserService getUserService(EventsService eventsService, RefreshTokenService refreshTokenService,
                                      MailResetPasswordService mailResetPasswordService, Gson gson) {
        return new DefaultUserService(getUserRepository(), eventsService, userTokenRepository, authorizationRulesRepository,
                refreshTokenService, mailResetPasswordService, gson);
    }

    @Bean
    public MailResetPasswordService getMailResetPasswordService(EventsService eventsService, ScopeService scopeService,
                                                                TokenFactory tokenFactory, ClientRepository clientRepository) {
        return new DefaultMailResetPasswordService(eventsService, scopeService, tokenFactory, clientRepository, env.getProperty(
                "iam.token.resetPasswordTokenScope", String.class), Clock.systemUTC(), env.getProperty(
                "iam.token.resetPasswordTokenDurationInSec", Long.class),
                env.getProperty("email.resetPassword.notification", String.class), env.getProperty("email.resetPassword.clientUrl",
                String.class));
    }

    @Bean
    public EventsService getEventsService(EventBus eventBus) {
        return new DefaultEventsService(eventBus);

    }

    @Bean
    public GroupService getGroupService() {
        return new DefaultGroupService(groupRepository, scopeRepository);
    }

    @Bean
    public AuthorizationProviderFactory getAuthorizationProviderFactory() {
        return new SpringAuthorizationProviderFactory();
    }

    @Bean(name = "facebook")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Provider getFacebookProvider() {
        return new FacebookProvider(identityRepository);

    }

    @Bean(name = "twitter")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Provider getTwitterProvider() {
        return new TwitterProvider(identityRepository);

    }

    @Bean(name = "google")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Provider getGoogleProvider() {
        return new GoogleProvider(identityRepository);
    }

    @Bean(name = "oauth-server")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Provider getCorbelProvider() {
        return new OAuthServerProvider(identityRepository);
    }

    private List<AuthorizationRule> getAuthorizationRules(ScopesAuthorizationRule scopesAuthorizationRule) {
        return Arrays.asList(new PrincipalExistsAuthorizationRule(), new VersionAuthorizationRule(), new RequestDomainAuthorizationRule(),
                scopesAuthorizationRule,
                new MaxExpireAuthorizationRule(env.getProperty("iam.auth.token.maxExpirationInMillis", Integer.class)),
                new ClientSideAuthenticationAllowedAuthorizationRule(), new AuthenticationTypeAllowedAuthorizationRule());
    }

    @Bean
    public ScopesAuthorizationRule getScopesAuthorizationRule(ScopeService scopeService, GroupService groupService) {
        return new ScopesAuthorizationRule(scopeService, groupService);
    }

    @Bean
    public JsonTokenParser getAuthorizationJsonTokenParser() {
        return new JsonTokenParser(getVerifierProviders(), getAudienceChecker());
    }

    @Bean
    public JsonTokenParser getTokenUpgradeJsonTokenParser() {
        return new JsonTokenParser(getUpgradeTokenVerifierProviders(), getAudienceChecker());
    }

    @Bean
    public IamShell getIamShell(GroupService groupService) {
        return new IamShell(clientRepository, scopeRepository, getUserRepository(), domainRepository, env.getProperty("iam.uri"),
                identityRepository, groupRepository);
    }

    @Bean
    public ScopeService getScopeService(EventsService eventsService) {
        return new DefaultScopeService(scopeRepository, authorizationRulesRepository, env.getProperty(
                "iam.auth.publicToken.maxExpirationInMillis", Long.class), getScopeFillStrategy(), env.getProperty("iam.uri"),
                Clock.systemDefaultZone(), eventsService);
    }

    @Bean
    public IdGeneratorMongoEventListener<Client> getClientIdGeneratorMongoEventListener() {
        return new IdGeneratorMongoEventListener<>(getClientIdGenerator(), Client.class);
    }

    @Bean
    public IdGeneratorMongoEventListener<Identity> getIdentityIdGeneratorMongoEventListener() {
        return new IdGeneratorMongoEventListener<>(getIdentityIdGenerator(), Identity.class);
    }

    @Bean
    public IdGeneratorMongoEventListener<Device> getDeviceIdGeneratorMongoEventListener() {
        return new IdGeneratorMongoEventListener<>(getDeviceIdGenerator(), Device.class);
    }

    @Bean
    public IdGeneratorMongoEventListener<Group> geGroupIdGeneratorMongoEventListener() {
        return new IdGeneratorMongoEventListener<>(getGroupIdGenerator(), Group.class);
    }

    @Bean
    public DomainService getDomainService(ScopeService scopeService, EventsService eventsService, AggregationResultsFactory<JsonElement> aggregationResultsFactory) {
        return new DefaultDomainService(domainRepository, scopeService, eventsService, aggregationResultsFactory);
    }

    @Bean
    public UpgradeTokenService getUpgradeTokenService(ScopeService scopeService) {
        return new DefaultUpgradeTokenService(getTokenUpgradeJsonTokenParser(), scopeService, userTokenRepository);
    }

    @Bean
    public TokenCookieFactory getSessionCookieFactory() {
        return new DefaultTokenCookieFactory(env.getProperty("token.cookie.path"), env.getProperty("token.cookie.domain"),
                env.getProperty("token.cookie.comment"), env.getProperty("token.cookie.secure", Boolean.class));
    }

    @Bean
    public QueryParser getQueryParser() {
        return new JacksonQueryParser(getCustomJsonParser());
    }

    @Bean
    public CustomJsonParser getCustomJsonParser() {
        return new CustomJsonParser(getObjectMapper().getFactory());
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JSR310Module());
        return mapper;
    }

    @Bean
    public Gson getGson() {
        return new Gson();
    }

    @Bean
    public AggregationResultsFactory<JsonElement> aggregationResultsFactory(Gson gson){
        return new JsonAggregationResultsFactory(gson);
    }

    private IdGenerator<Client> getClientIdGenerator() {
        return new ClientIdGenerator(DigesterFactory.murmur3_32());
    }

    private IdGenerator<Identity> getIdentityIdGenerator() {
        return new IdentityIdGenerator();
    }

    private IdGenerator<Group> getGroupIdGenerator() {
        return new GroupIdGenerator(DigesterFactory.murmur3_32());
    }

    @Bean
    public IdGenerator<Device> getDeviceIdGenerator() {
        return new DeviceIdGenerator();
    }

    private ScopeFillStrategy getScopeFillStrategy() {
        return new MustacheScopeFillStrategy();
    }

    private AuthorizationRequestContextFactory getAuthorizationRequestContextFactory(ScopeService scopeService) {
        return new AuthorizationRequestContextFactory(clientRepository, domainRepository, getUserRepository());
    }

    private Checker getAudienceChecker() {
        return new SignedJsonAssertionAudienceChecker(env.getProperty("iam.uri"));
    }

    private VerifierProviders getVerifierProviders() {
        VerifierProviders providers = new VerifierProviders();
        providers.setVerifierProvider(SignatureAlgorithm.HS256, getClientVerifierProvider());
        providers.setVerifierProvider(SignatureAlgorithm.RS256, getClientVerifierProvider());
        return providers;
    }

    private VerifierProviders getUpgradeTokenVerifierProviders() {
        VerifierProviders providers = new VerifierProviders();
        providers.setVerifierProvider(SignatureAlgorithm.HS256, getTokenUpgradeVerifierProvider());
        return providers;
    }

    private VerifierProvider getClientVerifierProvider() {
        return new ClientVerifierProvider(clientRepository);
    }

    private VerifierProvider getTokenUpgradeVerifierProvider() {
        return new TokenUpgradeVerifierProvider(env.getProperty("assets.upgradeSignerKey"));
    }
}
