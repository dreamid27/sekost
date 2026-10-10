package id.my.selo.sekost.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import id.my.selo.sekost.core.data.database.dao.IssueDao
import id.my.selo.sekost.core.data.database.dao.PaymentDao
import id.my.selo.sekost.core.data.database.dao.PropertyDao
import id.my.selo.sekost.core.data.database.dao.RoomDao
import id.my.selo.sekost.core.data.database.dao.TenantDao
import id.my.selo.sekost.core.data.database.entity.IssueEntity
import id.my.selo.sekost.core.data.database.entity.PaymentEntity
import id.my.selo.sekost.core.data.database.entity.PropertyEntity
import id.my.selo.sekost.core.data.database.entity.RoomEntity
import id.my.selo.sekost.core.data.database.entity.TenantEntity

@Database(
    entities = [PropertyEntity::class, RoomEntity::class, TenantEntity::class, PaymentEntity::class, IssueEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SekostDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
    abstract fun roomDao(): RoomDao
    abstract fun tenantDao(): TenantDao
    abstract fun paymentDao(): PaymentDao
    abstract fun issueDao(): IssueDao

    companion object {
        const val NAME = "sekost.db"
    }
}
