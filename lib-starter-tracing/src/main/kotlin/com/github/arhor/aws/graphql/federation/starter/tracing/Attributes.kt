package com.github.arhor.aws.graphql.federation.starter.tracing

const val IDEMPOTENT_KEY = "x-idempotency-key"
const val TRACING_ID_KEY = "x-tracing-id"
const val REQUEST_ID_KEY = "x-request-id"

enum class Attributes(val key: String) {
    TRACING_ID(key = TRACING_ID_KEY),
    REQUEST_ID(key = REQUEST_ID_KEY),
    ;
}
