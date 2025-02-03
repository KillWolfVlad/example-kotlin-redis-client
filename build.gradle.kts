plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinter)
}

group = "ru.killwolfvlad"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor)

    implementation(libs.bundles.kotlinx)

    testImplementation(libs.bundles.kotest)

    testImplementation(libs.bundles.jedis)
}

tasks.check {
    dependsOn("installKotlinterPrePushHook")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
