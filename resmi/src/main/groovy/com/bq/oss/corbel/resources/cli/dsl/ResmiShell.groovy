package com.bq.oss.corbel.resources.cli.dsl

import org.springframework.data.mongodb.core.index.Index

import com.bq.oss.corbel.resources.rem.model.ResourceUri
import com.bq.oss.corbel.resources.rem.model.SearchResource
import com.bq.oss.corbel.resources.rem.search.ResmiSearch
import com.bq.oss.corbel.resources.rem.service.ResmiService
import io.corbel.lib.cli.console.Description
import io.corbel.lib.cli.console.Shell
import com.google.gson.JsonObject
import com.google.gson.JsonParser

@Shell("resmi")
class ResmiShell {

    ResmiService resmiService
    ResmiSearch resmiSearch

    @Description("Creates a mongo expiration index named \"_expireAt\" on the specified collection .")
    def ensureExpireIndex(String collection) {
        resmiService.ensureExpireIndex(new ResourceUri(collection))
    }

    @Description("Creates a mongo index on the specified collection. See <> for documentation on index syntax")
    def ensureIndex(String collection, Index index) {
        resmiService.ensureIndex(new ResourceUri(collection), index)
    }

    @Description("Creates a mongo index on the specified relation. See <> for documentation on index syntax")
    def ensureIndex(String collection, String relation, Index index) {
        resmiService.ensureIndex(new ResourceUri(collection).setRelation(relation), index)
    }

    @Description("Upsert a resource in RESMI.")
    def upsert(String type, JsonObject json) {
        resmiService.saveResource(new ResourceUri(type), json, Optional.empty())
    }

    @Description("Construct a json object from string. Example: resmi.json('{\"a\", \"b\"}')")
    def json(String json) {
        new JsonParser().parse(json)
    }

    @Description("Full text search fields in a type.")
    def searchableFields(String type, String... fields) {
        assert type: "type is required"
        assert fields: "fields is required"
        resmiService.addSearchableFields(new SearchResource(type, fields.collect().toSet()))
    }

    @Description("Full text search fields in a type.")
    def searchableRelationFields(String type, String relation, String... fields) {
        assert type: "type is required"
        assert relation: "relation is required"
        assert fields: "fields is required"
        resmiService.addSearchableFields(new SearchResource(type, relation, fields.collect().toSet()))
    }

    @Description("Full text search fields in a type.")
    def defineIndex(String type, String relation, JsonObject mapping) {
        assert type: "type is required"
        assert mapping: "mapping is required"
        resmiSearch.setupMapping(new ResourceUri(type, null, relation), mapping)
    }

    def index = IndexBuilder.index
}
