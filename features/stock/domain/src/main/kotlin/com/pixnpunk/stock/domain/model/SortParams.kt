package com.pixnpunk.stock.domain.model

enum class SortBy(val apiName: String) {
    CREATED("created"),
    UPDATED("updated"),
    COMMENTS("comments"),
    LIKES("likes"),
    DOWNLOADS("downloads"),
    PLAYS("plays"),
    EMPTY("")
}

enum class SortOrder(val apiName: String) {
    ASCENDING("asc"),
    DESCENDING("desc")
}
