import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

group = "me.valer"
version = "1.0-SNAPSHOT"


val tgApi = "6.3.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.7.20")
    testImplementation(kotlin("test"))
    implementation("org.telegram:telegrambots-abilities:$tgApi")
    implementation("org.postgresql:postgresql:42.5.1")
}


val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"
