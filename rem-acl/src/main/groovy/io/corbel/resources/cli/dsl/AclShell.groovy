package io.corbel.resources.cli.dsl

import io.corbel.event.ResourceEvent
import io.corbel.eventbus.service.EventBus
import io.corbel.lib.cli.console.Description
import io.corbel.lib.cli.console.Shell

@Shell("acl")
class AclShell {

    EventBus eventBus
    String aclAdminCollection

    @Description("Adds a managed collection to the rem registry")
    void addAclConfiguration(String domain, String collection) {
        eventBus.dispatch(ResourceEvent.createResourceEvent(aclAdminCollection, "$domain:$collection", domain, ""))
    }

    @Description("Removes a managed collection from the rem registry")
    void removeAclConfiguration(String domain, String collection) {
        eventBus.dispatch(ResourceEvent.deleteResourceEvent(aclAdminCollection, "$domain:$collection", domain, ""))
    }

}
