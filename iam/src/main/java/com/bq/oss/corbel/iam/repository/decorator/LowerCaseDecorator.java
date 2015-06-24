package com.bq.oss.corbel.iam.repository.decorator;

import java.util.List;
import java.util.stream.Collectors;

import org.bouncycastle.util.Strings;

import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.repository.UserRepository;
import com.bq.oss.lib.queries.ListQueryLiteral;
import com.bq.oss.lib.queries.StringQueryLiteral;
import com.bq.oss.lib.queries.request.AggregationResult;
import com.bq.oss.lib.queries.request.Pagination;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bq.oss.lib.queries.request.Sort;

/**
 * @author Francisco Sanchez
 */
public class LowerCaseDecorator extends UserRepositoryDecorator {

    public LowerCaseDecorator(UserRepository decoratedUserRepository) {
        super(decoratedUserRepository);
    }

    @Override
    public User findByDomainAndEmail(String domain, String email) {
        return decoratedUserRepository.findByDomainAndEmail(domain, Strings.toLowerCase(email));
    }

    @Override
    public List<User> find(ResourceQuery resourceQuery, Pagination pagination, Sort sort) {
        emailQueryToLowerCase(resourceQuery);
        return decoratedUserRepository.find(resourceQuery, pagination, sort);
    }

    @Override
    public AggregationResult count(ResourceQuery resourceQuery) {
        emailQueryToLowerCase(resourceQuery);
        return decoratedUserRepository.count(resourceQuery);
    }

    @Override
    public User save(User user) {
        emailUserToLowerCase(user);
        return decoratedUserRepository.save(user);
    }

    @Override
    public <S extends User> List<S> save(Iterable<S> users) {
        users.forEach(user -> emailUserToLowerCase(user));
        return decoratedUserRepository.save(users);
    }

    private void emailUserToLowerCase(User user) {
        user.setEmail(Strings.toLowerCase(user.getEmail()));
    }

    private void emailQueryToLowerCase(ResourceQuery resourceQuery) {
        resourceQuery.forEach(queryNode -> {
            if ("email".equals(queryNode.getField())) {
                try {
                    if (queryNode.getValue() instanceof ListQueryLiteral) {
                        ListQueryLiteral list = (ListQueryLiteral) queryNode.getValue();
                        list.setLiteral(list.getLiteral().stream()
                                .map(literal -> lowerCaseStringQueryLiteral((StringQueryLiteral) literal)).collect(Collectors.toList()));

                    } else {
                        lowerCaseStringQueryLiteral((StringQueryLiteral) queryNode.getValue());
                    }
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Email query must be string or array of strings.", e);
                }
            }

        });


    }

    private StringQueryLiteral lowerCaseStringQueryLiteral(StringQueryLiteral queryLiteral) {
        queryLiteral.setLiteral(Strings.toLowerCase(queryLiteral.getLiteral()));
        return queryLiteral;
    }

}
