import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.type
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
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


tasks.create("MyFatJar", Jar::class) {
    group = "my tasks" // OR, for example, "build"
    description = "Creates a self-contained fat JAR of the application that can be run."
    manifest.attributes["Main-Class"] = "MainKt"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/INDEX.LIST")
    from(dependencies)
    with(tasks.jar.get())
}



val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"
