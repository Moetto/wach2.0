import java.io.File
import java.io.FileInputStream
import java.io.IOException

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

val dockerBuild by tasks.registering {
    description = "Build a docker image"
    inputs.file("$buildDir/distributions/app.tar")
    inputs.file("Dockerfile")
    val imageHashFile = "$buildDir/image-hash"
    outputs.file(imageHashFile)
    outputs.upToDateWhen {
        val findHash = ProcessBuilder().command("sh", "-c", "docker image inspect wach:dev | jq '.[0].Id'").start()
        val foundImageHash = String(findHash.inputStream.readAllBytes()).trim()
        var previousHash: String? = null
        try {
            previousHash = String(FileInputStream(File(imageHashFile)).readAllBytes()).trim()
        } catch (e: IOException) {

        }
        foundImageHash == previousHash
    }
    doLast {
        exec {
            commandLine("docker", "build", ".", "-t", "wach:dev")
        }
        // Save the image hash so that gradle caches the step
        exec {
            commandLine("bash", "-c", "docker image inspect wach:dev | jq '.[0].Id' > build/image-hash")
        }
    }
}

tasks {
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

val dockerImageConfiguration: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add("dockerImageConfiguration", File("$buildDir/image-hash")) {
        builtBy(dockerBuild)
    }
}
