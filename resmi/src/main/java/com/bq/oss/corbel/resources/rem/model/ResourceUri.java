package com.bq.oss.corbel.resources.rem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Francisco Sanchez
 */
public class ResourceUri {

    public static final String WILDCARD_RESOURCE_ID = "_";

    private String type;
    private String typeId;
    private String relation;
    private String relationId;

    public ResourceUri() {}

    public ResourceUri(String type) {
        this(type, null);
    }

    public ResourceUri(String type, String typeId) {
        this(type, typeId, null);
    }

    public ResourceUri(String type, String typeId, String relation) {
        this(type, typeId, relation, null);
    }

    public ResourceUri(String type, String typeId, String relation, String relationId) {
        this.type = type;
        this.typeId = typeId;
        this.relation = relation;
        this.relationId = relationId;
    }

    public String getType() {
        return type;
    }

    public ResourceUri setType(String type) {
        this.type = type;
        return this;
    }

    public String getTypeId() {
        return typeId;
    }

    public ResourceUri setTypeId(String typeId) {
        this.typeId = typeId;
        return this;
    }

    public String getRelation() {
        return relation;
    }

    public ResourceUri setRelation(String relation) {
        this.relation = relation;
        return this;
    }

    public String getRelationId() {
        return relationId;
    }

    public ResourceUri setRelationId(String relationId) {
        this.relationId = relationId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ResourceUri))
            return false;

        ResourceUri that = (ResourceUri) o;

        if (relation != null ? !relation.equals(that.relation) : that.relation != null)
            return false;
        if (relationId != null ? !relationId.equals(that.relationId) : that.relationId != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null)
            return false;
        if (typeId != null ? !typeId.equals(that.typeId) : that.typeId != null)
            return false;

        return true;
    }

    public boolean isTypeWildcard() {
        return WILDCARD_RESOURCE_ID.equals(typeId);
    }

    public boolean isRelationWildcard() {
        return WILDCARD_RESOURCE_ID.equals(relationId);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (typeId != null ? typeId.hashCode() : 0);
        result = 31 * result + (relation != null ? relation.hashCode() : 0);
        result = 31 * result + (relationId != null ? relationId.hashCode() : 0);
        return result;
    }

    @JsonIgnore
    public boolean isCollection() {
        return type != null && typeId == null && relation == null;
    }

    @JsonIgnore
    public boolean isResource() {
        return type != null && typeId != null && relation == null;
    }

    @JsonIgnore
    public boolean isRelation() {
        return type != null && relation != null;
    }

}
