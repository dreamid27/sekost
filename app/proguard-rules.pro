# Keep kotlinx.serialization metadata for type-safe navigation routes.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers @kotlinx.serialization.Serializable class id.my.selo.sekost.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
