package io.corbel.resources.rem.dao;

import com.google.common.base.Joiner;
import org.springframework.http.MediaType;

/**
 * @author Alexander De Leon
 */
public class DefaultKeyNormalizer implements KeyNormalizer {

    public String normalize(MediaType mediaType, String collection, String resource) {
        String normalizedMediaType = mediaType.toString().replace("/", "_");
        if (resource.endsWith(normalizedMediaType) && resource.startsWith(collection)) {
            return resource;
        } else {
            return Joiner.on("/").join(collection, Joiner.on(".").join(resource, normalizedMediaType));
        }
    }
}
