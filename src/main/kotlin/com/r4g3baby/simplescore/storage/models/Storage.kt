package com.r4g3baby.simplescore.storage.models

data class Storage(
    private val _driver: String,
    val tablePrefix: String,
    val address: String,
    val database: String,
    val username: String,
    val password: String,
    val pool: Pool
) {
    val driver = Driver.fromValue(_driver)

    data class Pool(
        val maximumPoolSize: Int,
        val minimumIdle: Int,
        val maxLifetime: Long,
        val keepaliveTime: Long,
        val connectionTimeout: Long,
        val extraProperties: Map<String, Any>
    )
}
