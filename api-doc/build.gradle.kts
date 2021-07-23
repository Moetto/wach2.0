plugins {
    // Base plugin provides default lifecycle tasks and their dependency order
    base
    id("wach2.kotlin-common-conventions")
    id ("org.openapi.generator") version "5.2.0"
}

tasks {
    openApiValidate {
        inputSpec.set("$projectDir/openapi.yaml")
    }

    openApiGenerate {
        inputSpec.set("$projectDir/openapi.yaml")
        generatorName.set("html")
    }

    check {
        finalizedBy(openApiValidate)
    }

    build {
        finalizedBy(openApiGenerate)
    }
}
