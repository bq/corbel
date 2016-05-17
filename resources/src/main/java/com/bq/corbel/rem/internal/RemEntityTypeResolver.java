package com.bq.corbel.rem.internal;

import com.bq.corbel.resources.rem.Rem;

/**
 * @author Alexander De Leon
 * 
 */
public interface RemEntityTypeResolver {

    Class<?> getEntityType(Rem<?> rem);

}
