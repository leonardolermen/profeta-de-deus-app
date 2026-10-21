import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
val local = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun config(name: String) = (local.getProperty(name) ?: System.getenv(name) ?: "")
    .replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "").replace("\r", "")
android {
    namespace = "br.org.profetas.app"
    compileSdk = 36
    defaultConfig {
        // Identificador do aplicativo Expo preservado para permitir estudar uma atualizacao futura.
        applicationId = "com.jlpsd.telasongmtdgky8qavduwwg50shqb"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "2.0.0"
        buildConfigField("String", "SUPABASE_URL", "\"${config("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${config("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "BIBLE_API_TOKEN", "\"${config("BIBLE_API_TOKEN")}\"")
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildTypes { release { isMinifyEnabled = false } }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("androidx.core:core-ktx:1.16.0")
    testImplementation("junit:junit:4.13.2")
}
