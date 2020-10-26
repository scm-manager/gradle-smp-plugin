package com.cloudogu.smp

import org.gradle.api.Project

class TestTasks {

    static void configure(Project project) {
        project.tasks.getByName("test").configure {
            dependsOn("ui-tests")
        }
        project.afterEvaluate {
            project.test {
                useJUnitPlatform()
            }
        }
    }

}
