package com.bq.oss.corbel.event;

import java.util.List;

import com.bq.oss.corbel.eventbus.EventWithSpecificDomain;

public class DomainDeletedEvent extends EventWithSpecificDomain {
    private List<String> clientIds;

    public DomainDeletedEvent() {}

    public DomainDeletedEvent(String domain, List<String> clientIds) {
        super(domain);
        this.clientIds = clientIds;
    }

    public List<String> getClientIds() {
        return clientIds;
    }

    public void setClientIds(List<String> clientIds) {
        this.clientIds = clientIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DomainDeletedEvent))
            return false;
        if (!super.equals(o))
            return false;

        DomainDeletedEvent that = (DomainDeletedEvent) o;

        return !(clientIds != null ? !clientIds.equals(that.clientIds) : that.clientIds != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (clientIds != null ? clientIds.hashCode() : 0);
        return result;
    }
}
