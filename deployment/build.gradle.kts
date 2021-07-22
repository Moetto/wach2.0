plugins {
    // Base plugin provides default lifecycle tasks and their dependency order
    base
}

tasks {
    val lintCharts by registering {
        description = "Lint helm charts"
        group = "Verification"
        inputs.dir("wach/templates")
        val output = "$buildDir/helm-lint-results.txt"
        outputs.file(output)
        doLast {
            val res = exec {
                commandLine("helm", "lint", "../deployment/wach")
                standardOutput = java.io.FileOutputStream(output)
                isIgnoreExitValue = true
            }
            if(res.exitValue != 0){
                throw GradleException("Helm lint failed. Check $output for details")
            }
        }
    }

    check {
        finalizedBy(lintCharts)
    }
}
