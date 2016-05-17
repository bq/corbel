package com.bq.corbel.oauth.repository;

import org.springframework.data.repository.CrudRepository;

import com.bq.corbel.oauth.model.Client;

/**
 * @author Rubén Carrasco
 */
public interface ClientRepository extends CrudRepository<Client, String> {
    public Client findByName(String name);
}
