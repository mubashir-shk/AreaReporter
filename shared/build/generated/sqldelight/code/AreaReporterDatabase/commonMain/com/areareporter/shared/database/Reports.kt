package com.areareporter.shared.database

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Reports(
  public val id: String,
  public val title: String,
  public val description: String,
  public val category: String,
  public val latitude: Double,
  public val longitude: Double,
  public val imagePath: String?,
  public val status: String,
  public val createdAt: Long,
  public val retryCount: Long,
  public val lastSyncAttempt: Long?,
)
