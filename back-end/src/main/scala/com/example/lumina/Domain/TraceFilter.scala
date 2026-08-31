package com.example.lumina.Domain

import java.time.OffsetDateTime

case class TraceFilter(
    pagination: Pagination,
    status: Option[String],
    from: Option[OffsetDateTime],
    to: Option[OffsetDateTime]
)
