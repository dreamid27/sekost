package id.my.selo.sekost.core.domain

import id.my.selo.sekost.core.model.Issue
import id.my.selo.sekost.core.model.IssueStatus
import id.my.selo.sekost.core.model.IssueType
import id.my.selo.sekost.core.model.Payment
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.Room
import id.my.selo.sekost.core.model.RoomStatus
import id.my.selo.sekost.core.model.Tenant

/** Everything a list row needs to know about a room for a given billing period. */
data class RoomOverview(
    val room: Room,
    val property: Property?,
    val tenant: Tenant?,
    val fee: ResolvedFee,
    val payment: Payment?,
    val openMaintenance: Int,
    val openComplaints: Int,
) {
    val isOccupied: Boolean get() = room.status == RoomStatus.OCCUPIED
    val isPaid: Boolean get() = payment != null
    val needsPayment: Boolean get() = isOccupied && !isPaid
    val openIssues: Int get() = openMaintenance + openComplaints
}

/** Joins the raw tables into [RoomOverview]s. Pure, so it's trivially unit-testable. */
fun buildRoomOverviews(
    rooms: List<Room>,
    properties: List<Property>,
    activeTenants: List<Tenant>,
    periodPayments: List<Payment>,
    openIssues: List<Issue>,
    globalFee: Long,
): List<RoomOverview> {
    val propertyById = properties.associateBy { it.id }
    val tenantByRoom = activeTenants.filter { it.isActive }.associateBy { it.roomId }
    val paymentByRoom = periodPayments.associateBy { it.roomId }
    val issuesByRoom = openIssues.filter { it.status == IssueStatus.OPEN }.groupBy { it.roomId }
    return rooms.map { room ->
        val property = propertyById[room.propertyId]
        val issues = issuesByRoom[room.id].orEmpty()
        RoomOverview(
            room = room,
            property = property,
            tenant = tenantByRoom[room.id],
            fee = FeeResolver.resolve(room, property, globalFee),
            payment = paymentByRoom[room.id],
            openMaintenance = issues.count { it.type == IssueType.MAINTENANCE },
            openComplaints = issues.count { it.type == IssueType.COMPLAINT },
        )
    }
}
