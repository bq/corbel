package com.bq.corbel.iam.repository;

import com.bq.corbel.iam.model.Client;
import com.bq.corbel.iam.model.ClientCredential;

/**
 * @author Alexander De Leon
 * 
 */
public interface ClientRepositoryCustom {

    ClientCredential findCredentialById(String id);

    void delete(String domain, String client);

    void insert(Client client);

    void deleteByDomain(String domain);

}
