package com.cloudogu.smp

import org.gradle.api.Project

class TestTasks {

    static void configure(Project project) {
        project.afterEvaluate {
            project.test {
                useJUnitPlatform()
            }
        }
    }

}
