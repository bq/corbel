package io.corbel.resources.rem.eventbus;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpMethod;

import io.corbel.event.ResourceEvent;
import io.corbel.eventbus.EventHandler;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.model.RemDescription;
import io.corbel.resources.rem.service.RemService;

public class AclConfigurationEventHandler implements EventHandler<ResourceEvent> {

    private final List<Pair<Rem, HttpMethod>> remsAndMethods;
    private RemService remService;
    private final Pattern collectionPattern = Pattern.compile("^(?:.*/)?[\\w-_]+?(?::(?<collection>[\\w-_:]+))?$");
    private final String aclAdminCollection;

    public AclConfigurationEventHandler(List<Pair<Rem, HttpMethod>> remsAndMethods, String aclAdminCollection) {
        this.remsAndMethods = remsAndMethods;
        this.aclAdminCollection = aclAdminCollection;
    }

    public void setRemService(RemService remService) {
        this.remService = remService;
    }

    @Override
    public void handle(ResourceEvent event) {
        if (!event.getType().equals(aclAdminCollection)) {
            return;
        }

        switch (event.getAction()) {
            case CREATE:
                extractUriPattern(event.getResourceId()).ifPresent(this::addAclConfiguration);
                break;
            case DELETE:
                extractUriPattern(event.getResourceId()).ifPresent(this::removeAclConfiguration);
                break;
        }
    }

    @Override
    public Class<ResourceEvent> getEventType() {
        return ResourceEvent.class;
    }

    private Optional<String> extractUriPattern(String resourceId) {
        Matcher matcher = collectionPattern.matcher(resourceId);

        if (!matcher.matches()) {
            return Optional.empty();
        }

        return Optional.ofNullable(matcher.group("collection")).filter(c -> !c.isEmpty());
    }

    private void addAclConfiguration(String collection) {
        List<RemDescription> remDescriptions = remService.getRegisteredRemDescriptions();

        boolean alreadyRegistered = remDescriptions.stream()
                .anyMatch(description -> description.getUriPattern().equals(collection) && description.getRemName().startsWith("Acl"));

        if (alreadyRegistered) {
            return;
        }

        remsAndMethods.forEach(remAndMethod -> remService.registerRem(remAndMethod.getLeft(), collection, remAndMethod.getRight()));
    }

    private void removeAclConfiguration(String collection) {
        remsAndMethods.stream().map(Pair::getLeft).forEach(aclRem -> remService.unregisterRem(aclRem.getClass(), collection));
    }

}
