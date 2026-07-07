// Top-level build file — plugins declared here are applied per-module with `apply false`.
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
}

apply(plugin = "io.gitlab.arturbosch.detekt")

configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
    toolVersion = "1.23.6"
    config.setFrom(files("$rootDir/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
