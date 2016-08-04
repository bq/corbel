package com.bq.corbel.iam.model;

import org.springframework.data.annotation.Transient;

/**
 * @author Cristian del Cerro
 */
public class UserWithIdentityAndOptionalNotification extends User {

    @Transient private Identity identity;
    private boolean avoidNotification;

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public Identity getIdentity() {
        if (identity == null) {
            return null;
        }
        Identity copy = new Identity(identity);
        synchronizeIdentityWithUser(copy);
        return copy;
    }

    public boolean isAvoidNotification() {
        return avoidNotification;
    }

    public void setAvoidNotification(boolean avoidNotification) {
        this.avoidNotification = avoidNotification;
    }

    private void synchronizeIdentityWithUser(Identity identity) {
        identity.setDomain(this.getDomain());
        identity.setUserId(this.getId());
    }
}
