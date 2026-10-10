package id.my.selo.sekost.navigation

import kotlinx.serialization.Serializable

@Serializable data object HomeRoute

@Serializable data object BillingRoute

@Serializable data object IssuesRoute

@Serializable data object SettingsRoute

@Serializable data class PropertyRoute(val propertyId: Long)

@Serializable data class RoomRoute(val roomId: Long)

/*
 * Type-safe navigation stores route properties in SavedStateHandle under their property names.
 * ViewModels read them by key (instead of `toRoute()`, which needs a real Bundle) so they stay
 * constructible in plain JVM unit tests.
 */
internal fun androidx.lifecycle.SavedStateHandle.requireLong(key: String): Long =
    checkNotNull(get<Long>(key)) { "Missing navigation argument '$key'" }
