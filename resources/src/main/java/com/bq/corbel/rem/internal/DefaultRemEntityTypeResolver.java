package com.bq.corbel.rem.internal;

import com.bq.corbel.resources.rem.Rem;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultRemEntityTypeResolver implements RemEntityTypeResolver {

    @Override
    public Class<?> getEntityType(Rem<?> rem) {
        return rem.getType();
    }

}
