import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

val properties = Properties().apply {
    val propertiesFile = file("$rootDir/local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.frommetoyou.soundforme"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.frommetoyou.soundforme"
        minSdk = 24
        targetSdk = 35
        versionCode = 76
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        signingConfigs {
            create("release") {
                storeFile = file(properties["STORE_FILE"] as String)
                storePassword = properties["STORE_PASSWORD"] as String
                keyAlias = properties["KEY_ALIAS"] as String
                keyPassword = properties["KEY_PASSWORD"] as String
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationIdSuffix = ".free"
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".free"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    aaptOptions {
        noCompress  += "tflite"
        ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc:.*:!CVS:!thumbs.db:!picasa.ini:!*~"
    }

    androidComponents.onVariants { variant ->
        if (variant.buildType == "release") {
            afterEvaluate {
                tasks.named("assemble${variant.name.capitalize()}").configure {
                    doLast {
                        val mappingFile = file("${layout.buildDirectory}/outputs/mapping/release/mapping.txt")
                        val destDir = file("${rootDir}/mappings/${project.android.defaultConfig.versionName}")
                        copy {
                            from(mappingFile)
                            into(destDir)
                        }
                    }
                }
            }
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.compose.ui.text.google.fonts)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.task.audio)
    implementation(libs.androidx.datastore)
//    implementation(libs.tensorflow.lite.support)
//    implementation(libs.tensorflow.lite.task.vision)
    api(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.google.accompanist.permissions)
    implementation(libs.google.app.update)
    implementation(libs.google.app.update.ktx)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.ads)
    implementation(libs.core.splashscreen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}