plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.spotless)
}

// Mirrors .editorconfig so the CLI and the IDE agree on formatting.
val ktlintRules = mapOf(
    "ktlint_code_style" to "android_studio",
    "max_line_length" to "140",
    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
    "ktlint_standard_function-signature" to "disabled",
    "ktlint_standard_class-signature" to "disabled",
    "ktlint_standard_property-naming" to "disabled",
    "ktlint_standard_trailing-comma-on-call-site" to "disabled",
    "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
)

spotless {
    val ktlintVersion = libs.versions.ktlint.get()
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint(ktlintVersion).editorConfigOverride(ktlintRules)
    }
    kotlinGradle {
        target("**/*.kts")
        targetExclude("**/build/**")
        ktlint(ktlintVersion).editorConfigOverride(ktlintRules)
    }
    format("xml") {
        target("**/src/**/*.xml")
        targetExclude("**/build/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
