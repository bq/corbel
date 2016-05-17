package com.bq.corbel.iam.service;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.bq.corbel.iam.exception.ClientAlreadyExistsException;
import com.bq.corbel.iam.exception.InvalidAggregationException;
import com.bq.corbel.iam.model.Client;
import com.bq.corbel.lib.queries.request.*;

public interface ClientService {
    void createClient(Client client) throws ClientAlreadyExistsException;

    void update(Client client);

    void delete(String domain, String client);

    Optional<Client> find(String clientId);

    List<Client> findClientsByDomain(String domainId, ResourceQuery query, Pagination pagination, Sort sort);

    JsonElement getClientsAggregation(String domainId, ResourceQuery query, Aggregation aggregation)
            throws InvalidAggregationException;
}
