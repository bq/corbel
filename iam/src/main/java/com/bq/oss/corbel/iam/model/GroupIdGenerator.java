package com.bq.oss.corbel.iam.model;

import com.bq.oss.lib.mongo.IdGenerator;
import com.bq.oss.lib.ws.digest.Digester;
import com.google.common.base.Joiner;

public class GroupIdGenerator implements IdGenerator<Group> {

    private final static char SEPARATOR = ':';

    private final Digester digester;

    public GroupIdGenerator(Digester digester) {
        this.digester = digester;
    }

    @Override
    public String generateId(Group entity) {
        return digester.digest(Joiner.on(SEPARATOR).join(entity.getDomain(), entity.getName()));
    }

}
