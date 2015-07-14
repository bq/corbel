package com.bq.oss.corbel.event;

import com.bq.oss.corbel.eventbus.EventWithSpecificDomain;
import com.bq.oss.lib.token.TokenInfo;

/**
 * @author Alberto J. Rubio
 */
public class ResourceEvent extends EventWithSpecificDomain {

    public enum Action {
        CREATE, UPDATE, DELETE
    }

    private String type;
    private String resourceId;
    private Action action;
    private String userId;

    private ResourceEvent() {
        /**
         * Jackson2JsonMessageConverter requires this empty constructor
         */
    }

    private ResourceEvent(String type, String resourceId, Action action, TokenInfo tokenInfo) {
        super(tokenInfo.getDomainId());
        this.type = type;
        this.resourceId = resourceId;
        this.action = action;
        this.userId = tokenInfo.getUserId();
    }

    private ResourceEvent(String type, String resourceId, String domain, Action action, String userId) {
        super(domain);
        this.type = type;
        this.resourceId = resourceId;
        this.action = action;
        this.userId = userId;
    }

    public static ResourceEvent createResourceEvent(String type, String resourceId, TokenInfo tokenInfo) {
        return new ResourceEvent(type, resourceId, Action.CREATE, tokenInfo);
    }

    public static ResourceEvent updateResourceEvent(String type, String resourceId, TokenInfo tokenInfo) {
        return new ResourceEvent(type, resourceId, Action.UPDATE, tokenInfo);
    }

    public static ResourceEvent deleteResourceEvent(String type, String resourceId, TokenInfo tokenInfo) {
        return new ResourceEvent(type, resourceId, Action.DELETE, tokenInfo);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ResourceEvent other = (ResourceEvent) obj;
        if (action != other.action) {
            return false;
        }
        if (resourceId == null) {
            if (other.resourceId != null) {
                return false;
            }
        } else if (!resourceId.equals(other.resourceId)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

}
