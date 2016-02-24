package io.corbel.notifications.model;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class NotificationConfigByDomain {

    @Id
    private String id;

    @NotNull
    private String domain;

    @NotNull
    private String template;

    @NotEmpty
    private Map<String, String> properties;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void updateNotificationConfigByDomain(NotificationConfigByDomain notificationConfigByDomain) {
        if(notificationConfigByDomain.getDomain() != null) {
            setDomain(notificationConfigByDomain.getDomain());
        }
        if(notificationConfigByDomain.getTemplate()!= null) {
            setTemplate(notificationConfigByDomain.getTemplate());
        }
        if(notificationConfigByDomain.getProperties() != null && !notificationConfigByDomain.getProperties().isEmpty()) {
            setProperties(notificationConfigByDomain.getProperties());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationConfigByDomain that = (NotificationConfigByDomain) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (domain != null ? !domain.equals(that.domain) : that.domain != null) return false;
        if (template != null ? !template.equals(that.template) : that.template != null) return false;
        return !(properties != null ? !properties.equals(that.properties) : that.properties != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (template != null ? template.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }
}