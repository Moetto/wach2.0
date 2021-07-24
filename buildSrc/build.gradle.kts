plugins {
    // Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    // The plugin is not released with OAS3 support, even though it exists in master
    implementation(files("/home/t3mu/Projects/gradle-swagger-generator-plugin/build/libs/gradle-swagger-generator-plugin-SNAPSHOT.jar"))
    // Used by the plugin
    implementation("com.github.fge:json-schema-validator:2.2.6")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.4")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "15"
    }
}
