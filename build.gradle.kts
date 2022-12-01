import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "me.valer"
version = "1.0"
val tgApi = "6.3.0"



repositories {
    mavenCentral()
    maven { url = uri("https://jcenter.bintray.com/") }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.7.20")
    testImplementation(kotlin("test"))
    implementation("org.telegram:telegrambots-abilities:$tgApi")
    implementation("org.postgresql:postgresql:42.5.1")
    implementation("khttp:khttp:1.0.0")
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}

tasks.jar {
    manifest.attributes["Main-Class"] = "MainKt"
}

tasks.withType<org.gradle.jvm.tasks.Jar>() {
    exclude("META-INF/*.RSA", "META-INF/*.DSA", "META-INF/*.SF")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"
