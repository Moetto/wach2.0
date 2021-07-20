plugins {
    id("wach2.kotlin-application-conventions")
}

dependencies {
    implementation("io.vertx:vertx-core")
    implementation("io.vertx:vertx-web")
    implementation("io.vertx:vertx-lang-kotlin")

    testImplementation("io.vertx:vertx-junit5")
    testImplementation("io.vertx:vertx-web-client")
}

application {
    mainClass.set("dev.t3mu.wach.ServerKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "15"
    }
}


tasks {
    val dockerBuild by registering {
        description = "Build a docker image"
        outputs.cacheIf { true }
        inputs.file("build/distributions/app.tar")
        inputs.file("Dockerfile")
        outputs.file("build/image-hash")
        doLast {
            exec {
                commandLine("docker", "build", ".", "-t", "wach:dev")
            }
            // Save the image hash so that gradle caches the step
            exec {
                commandLine ("bash", "-c", "docker image inspect wach:dev | jq '.[0].Id' > build/image-hash")
            }
        }
    }
    build {
        finalizedBy(dockerBuild)
    }
    register("publish") {
        description = "Publish the docker image built by step build. Use repository from settings.gradle."
        dependsOn(":app:dockerBuild")
        inputs.property("repository", "t3mu/wach")
        inputs.property("tag", "dev")
        doLast {
            val repository = inputs.properties["repository"]
            val tag = inputs.properties["tag"]
            exec {
                commandLine("docker", "tag", "wach:dev", "$repository:$tag")
            }
            exec {
                commandLine("docker", "push", "$repository:$tag")
            }
        }
    }
}
