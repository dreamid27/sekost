package id.my.selo.sekost.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.PropertyType

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: PropertyType,
    val address: String,
    val defaultMonthlyFee: Long?,
)

fun PropertyEntity.asModel() = Property(id, name, type, address, defaultMonthlyFee)

fun Property.asEntity() = PropertyEntity(id, name.trim(), type, address.trim(), defaultMonthlyFee)
