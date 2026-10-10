package id.my.selo.sekost.core.data.repository

import id.my.selo.sekost.core.domain.TimeProvider
import id.my.selo.sekost.core.model.Issue
import id.my.selo.sekost.core.model.IssueType
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.PropertyType
import id.my.selo.sekost.core.model.Room
import id.my.selo.sekost.core.model.RoomStatus
import javax.inject.Inject

/** Fills an empty install with a realistic portfolio so new users can explore the app. */
class SampleDataSeeder @Inject constructor(
    private val properties: PropertyRepository,
    private val rooms: RoomRepository,
    private val tenants: TenantRepository,
    private val payments: PaymentRepository,
    private val issues: IssueRepository,
    private val time: TimeProvider,
    private val transactions: TransactionRunner,
) {
    /** Seeds once, atomically. Does nothing if the user already has data (e.g. a double tap). */
    suspend fun seed() = transactions.run {
        if (properties.count() > 0) return@run

        val today = time.today()
        val period = time.currentPeriod()

        val melati = properties.save(
            Property(name = "Kost Melati", type = PropertyType.KOST, address = "Jl. Kaliurang Km 5, Sleman", defaultMonthlyFee = 1_200_000),
        )
        val kenanga = properties.save(
            Property(name = "Rumah Kenanga", type = PropertyType.HOUSE, address = "Perum Griya Asri B-12, Bantul"),
        )

        val names = listOf("Rina Kartika", "Bagus Pratama", "Sari Wulandari", "Dimas Aditya", "Ayu Lestari")
        val melatiRooms = (1..8).map { n ->
            rooms.save(
                Room(
                    propertyId = melati,
                    label = "Kamar ${n.toString().padStart(2, '0')}",
                    floor = if (n <= 4) "Lantai 1" else "Lantai 2",
                    customMonthlyFee = if (n == 1 || n == 5) 1_500_000 else null,
                ),
            )
        }
        melatiRooms.take(5).forEachIndexed { i, roomId ->
            val tenantId = tenants.checkIn(roomId, names[i], "08123456${(70 + i * 3)}", today.minusMonths((i + 2).toLong()))
            if (i % 2 == 0) payments.markPaid(roomId, tenantId, period, if (i == 0) 1_500_000 else 1_200_000, today)
        }
        rooms.setStatus(melatiRooms[6], RoomStatus.MAINTENANCE)

        val kenangaRoom = rooms.save(Room(propertyId = kenanga, label = "Rumah Utama", customMonthlyFee = 2_750_000))
        tenants.checkIn(kenangaRoom, "Keluarga Hadi", "081398765432", today.minusMonths(10))

        issues.save(
            Issue(
                roomId = melatiRooms[6],
                type = IssueType.MAINTENANCE,
                title = "Kran kamar mandi bocor",
                description = "Air menetes terus, perlu ganti seal.",
                reportedOn = today.minusDays(2),
            ),
        )
        issues.save(
            Issue(
                roomId = melatiRooms[1],
                type = IssueType.COMPLAINT,
                title = "Wi-Fi sering putus",
                description = "Sinyal lemah di lantai 1 sejak minggu lalu.",
                reportedOn = today.minusDays(1),
            ),
        )
        issues.save(
            Issue(roomId = kenangaRoom, type = IssueType.MAINTENANCE, title = "Servis AC ruang tamu", reportedOn = today.minusDays(5)),
        )
    }
}
