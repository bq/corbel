package com.bq.corbel.resources.rem.service;

import com.bq.corbel.resources.rem.model.SearchResource;

import java.util.List;

/**
 * @author Rubén Carrasco
 *
 */
public interface SearchableFieldsService {

    List<SearchResource> getSearchableFields();

    void addSearchableFields(SearchResource searchResource);

}
