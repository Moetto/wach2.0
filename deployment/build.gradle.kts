import java.io.FileOutputStream
import java.io.ByteArrayOutputStream

plugins {
    // Base plugin provides default lifecycle tasks and their dependency order
    base
    id("wach2.kotlin-common-conventions")
}

dependencies {
    implementation(project(path = ":app", configuration = "dockerImageConfiguration"))
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
                standardOutput = FileOutputStream(output)
                isIgnoreExitValue = true
            }
            if (res.exitValue != 0) {
                throw GradleException("Helm lint failed. Check $output for details")
            }
        }
    }

    check {
        finalizedBy(lintCharts)
    }

    val createCluster by registering {
        description = "Create a kind cluster named wach"
        doLast {
            val res = exec {
                commandLine("sh", "-c", "kind get clusters | grep ^wach$")
                standardOutput = ByteArrayOutputStream()
                isIgnoreExitValue = true
            }
            if (res.exitValue == 0) {
                logger.info("""Cluster named "wach" already exists""")
            } else {
                exec {
                    workingDir = File("$projectDir")
                    commandLine("kind create cluster --name wach --config kind-cluster-config.yaml".split(" "))
                }
            }
        }
    }

    register("tearDownCluster") {
        description = "Tear down the kind cluster named wach"
        group = "Application"
        doLast {
            exec {
                workingDir = File("$projectDir")
                commandLine("kind delete cluster --name wach".split(" "))
            }
        }
    }

    val runInCluster by registering {
        description = "Create a kind cluster and install wach in it"
        group = "Application"
        dependsOn(build, createCluster)
        doLast {
            val helmHistory = exec {
                commandLine("helm history wach".split(" "))
                standardOutput = ByteArrayOutputStream()
                isIgnoreExitValue = true
            }
            if (helmHistory.exitValue == 0) {
                logger.info("Wach is already installed")
            } else {
                exec {
                    commandLine(
                        "sh", "-c",
                        """
                        docker tag wach:dev t3mu/wach:dev
                        kind load docker-image t3mu/wach:dev --name wach
                        """
                    )
                }
                exec {
                    workingDir = File("$projectDir")
                    commandLine("scripts/install-and-wait-for-nginx-ingress.sh")
                }
                exec {
                    workingDir = File("$(buildDir)/..")
                    commandLine("helm install wach wach".split(" "))
                }
            }
        }
    }

    register("integrationTest") {
        description = "Run integration tests against a local kind cluster"
        group = "Verification"
        dependsOn(runInCluster)
        val output = FileOutputStream("$buildDir/curl-output.txt")
        doLast {
            exec {
                commandLine("curl", "-vf", "--retry", "10", "--retry-all-errors", "localhost")
                standardOutput = output
                errorOutput = output
            }
        }
    }
}
