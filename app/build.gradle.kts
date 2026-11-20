plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

val huertoUrl = providers.gradleProperty("huertoUrl").orElse("http://10.0.2.2:8080/").get()
require(huertoUrl.none { it == '"' || it == '\\' || it.isWhitespace() } && huertoUrl.endsWith("/") && (huertoUrl.startsWith("http://") || huertoUrl.startsWith("https://"))) {
    "huertoUrl debe ser una URL HTTP(S) con barra final."
}
val rutaLlave = providers.gradleProperty("huertoKeystore").orNull
val claveDemo = providers.environmentVariable("HUERTO_KEY_PASSWORD").orNull

android {
    namespace = "cl.duoc.fotomotion"
    compileSdk {
        version = release(36) { minorApiLevel = 1 }
    }

    defaultConfig {
        applicationId = "cl.duoc.fotomotion"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
        buildConfigField("String", "HUERTO_URL", "\"$huertoUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (rutaLlave != null && claveDemo != null) {
            create("academica") {
                storeFile = rootProject.file(rutaLlave)
                storePassword = claveDemo
                keyAlias = "huerto-academico"
                keyPassword = claveDemo
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("academica")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.icons)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Room, Navigation y Coil
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.coil.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kover {
    reports {
        filters {
            includes {
                classes("cl.duoc.fotomotion.domain.HuertoStore*", "cl.duoc.fotomotion.domain.Reglas*")
            }
        }
        verify {
            rule("Cobertura de lógica de aplicación") {
                minBound(80)
            }
        }
    }
}
