package id.my.selo.sekost.core.model

import java.time.LocalDate

enum class IssueType { MAINTENANCE, COMPLAINT }

enum class IssueStatus { OPEN, RESOLVED }

/** A maintenance need or a tenant complaint attached to a room. */
data class Issue(
    val id: Long = 0,
    val roomId: Long,
    val type: IssueType,
    val title: String,
    val description: String = "",
    val status: IssueStatus = IssueStatus.OPEN,
    val reportedOn: LocalDate,
    val resolvedOn: LocalDate? = null,
)
