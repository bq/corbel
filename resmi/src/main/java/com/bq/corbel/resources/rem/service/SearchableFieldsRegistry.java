package com.bq.corbel.resources.rem.service;

import java.util.Set;

import com.bq.corbel.resources.rem.model.ResourceUri;
import com.bq.corbel.resources.rem.model.SearchResource;

/**
 * @author Francisco Sanchez
 */
public interface SearchableFieldsRegistry {
    Set<String> getFieldsFromType(String domain, String type);

    Set<String> getFieldsFromRelation(String domain, String type, String relation);

    void addFields(SearchResource searchResource);

    Set<String> getFieldsFromResourceUri(ResourceUri resourceUri);
}
