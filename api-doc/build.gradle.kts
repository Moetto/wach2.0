plugins {
    // Base plugin provides default lifecycle tasks and their dependency order
    base
    id("wach2.kotlin-common-conventions")
    id ("org.openapi.generator") version "5.2.0"
}

tasks {
    openApiValidate {
        inputSpec.set("$projectDir/swagger.yaml")
    }

    openApiGenerate {
        inputSpec.set("$projectDir/swagger.yaml")
        generatorName.set("kotlin-vertx")
    }

    check {
        dependsOn(openApiValidate)
    }

    build {
        finalizedBy(openApiGenerate)
    }
}
