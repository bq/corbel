package com.bq.oss.corbel.resources.rem.dao;

import org.springframework.http.MediaType;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.bq.oss.corbel.resources.rem.model.RestorObject;

/**
 * @author Alberto J. Rubio
 */
public class S3RestorDao implements RestorDao {

    private final KeyNormalizer keyNormalizer;
    private final AmazonS3 amazonS3Client;
    private final String bucket;
    private final Integer amazonS3Retries;
    private final String imageCacheCollection;

    public S3RestorDao(KeyNormalizer keyNormalizer, AmazonS3 amazonS3Client, String bucket, Integer retry, String imageCacheCollection) {
        this.keyNormalizer = keyNormalizer;
        this.amazonS3Client = amazonS3Client;
        this.bucket = bucket;
        this.amazonS3Retries = retry;
        this.imageCacheCollection = imageCacheCollection;
    }

    @Override
    public RestorObject getObject(MediaType mediaType, String collection, String resource) {
        return getObject(mediaType, collection, resource, 0);
    }

    private RestorObject getObject(MediaType mediaType, String collection, String resource, int retryNumber) {
        GetObjectRequest objectRequest = new GetObjectRequest(bucket, keyNormalizer.normalize(mediaType, collection, resource));
        try {
            S3Object s3Object = amazonS3Client.getObject(objectRequest);
            MediaType objectType = MediaType.parseMediaType(s3Object.getObjectMetadata().getContentType());
            return new RestorObject(objectType, s3Object.getObjectContent(), s3Object.getObjectMetadata().getContentLength());
        } catch (AmazonS3Exception e) {
            switch (e.getStatusCode()) {
                case 404:
                    return null;
                case 500:
                    if (retryNumber > amazonS3Retries) {
                        return getObject(mediaType, collection, resource, retryNumber + 1);
                    }
                default:
                    throw e;
            }
        }
    }

    @Override
    public void uploadObject(String collection, String resource, RestorObject object) {
        uploadObject(collection, resource, object, 0);
    }

    private void uploadObject(String collection, String resource, RestorObject object, int retryNumber) {
        MediaType mediaType = object.getMediaType();

        if (collection.equals(imageCacheCollection)) {
            invalidateImageCache(mediaType, resource);
        }

        PutObjectRequest objectRequest = new PutObjectRequest(bucket, keyNormalizer.normalize(object.getMediaType(), collection, resource),
                object.getInputStream(), createObjectMetadataForObject(object.getMediaType(), object.getContentLength()));
        try {
            amazonS3Client.putObject(objectRequest);
        } catch (AmazonS3Exception e) {
            switch (e.getStatusCode()) {
                case 500:
                    if (retryNumber > amazonS3Retries) {
                        uploadObject(collection, resource, object, retryNumber + 1);
                        break;
                    }
                default:
                    throw e;
            }
        }
    }

    @Override
    public void deleteObject(MediaType mediaType, String collection, String resource) {
        if (collection.equals(imageCacheCollection)) {
            invalidateImageCache(mediaType, resource);
        }

        deleteObject(mediaType, collection, resource, 0);
    }

    private void invalidateImageCache(MediaType mediaType, String resource) {
        amazonS3Client
                .listObjects(new ListObjectsRequest().withBucketName(bucket).withPrefix(resource))
                .getObjectSummaries()
                .stream()
                .map(S3ObjectSummary::getKey)
                .forEach(
                        key -> amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, keyNormalizer.normalize(mediaType,
                                imageCacheCollection, key))));
    }

    private void deleteObject(MediaType mediaType, String collection, String resource, int retryNumber) {
        try {
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, keyNormalizer.normalize(mediaType, collection, resource)));
        } catch (AmazonS3Exception e) {
            switch (e.getStatusCode()) {
                case 500:
                    if (retryNumber > amazonS3Retries) {
                        deleteObject(mediaType, collection, resource, retryNumber + 1);
                        break;
                    }
                default:
                    throw e;
            }
        }
    }

    private ObjectMetadata createObjectMetadataForObject(MediaType mediaType, Long contentLength) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(mediaType.toString());
        if (contentLength != null) {
            objectMetadata.setContentLength(contentLength);
        }
        return objectMetadata;
    }

}
