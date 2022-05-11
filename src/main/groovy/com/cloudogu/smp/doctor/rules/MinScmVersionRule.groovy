package com.cloudogu.smp.doctor.rules

import com.cloudogu.smp.doctor.Context
import com.cloudogu.smp.doctor.Result
import com.cloudogu.smp.doctor.Rule
import org.gradle.api.GradleException
import org.gradle.util.VersionNumber

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class MinScmVersionRule implements Rule {

  private static final String ERROR_MIN_VERSION = "The scmVersion is %s, but the plugin dependency %s requires at least %s"
  private static final String OK_MIN_VERSION = "Current scmVersion %s is newer or equal than %s, which is requested by depending plugins"

  private final Closure<Set<File>> dependencyCollector

  MinScmVersionRule() {
    this.dependencyCollector = { context ->
      return context.project.configurations.runtimePluginElements.resolvedConfiguration
        .resolvedArtifacts
        .findAll {
          it.extension == "smp"
        }
        .collect {
          it.file
        } as Set<File>
    }
  }

  MinScmVersionRule(Closure<Set<File>> dependencyCollector) {
    this.dependencyCollector = dependencyCollector
  }

  @Override
  Result validate(Context context) {
    Set<File> files = dependencyCollector.call(context)

    def plugins = files.collect { file ->
      createPlugin(file)
    }.findAll { p ->
      p != null
    }

    if (plugins.isEmpty()) {
      return Result.ok("no plugin dependencies with min-version condition found")
    }

    Collections.sort(plugins)

    Plugin pluginWithHighestMinVersion = plugins.last()
    VersionNumber currentVersion = VersionNumber.parse(context.extension.getScmVersion().get())
    if (currentVersion.compareTo(pluginWithHighestMinVersion.version) < 0) {
      String message = String.format(
        ERROR_MIN_VERSION, currentVersion, pluginWithHighestMinVersion.name, pluginWithHighestMinVersion.version
      )
      return Result.error(message).build()
    }

    String message = String.format(
      OK_MIN_VERSION, currentVersion, pluginWithHighestMinVersion.version
    )
    return Result.ok(message)
  }

  private static Plugin createPlugin(File smp) {
    ZipFile zip = new ZipFile(smp)
    ZipEntry entry = zip.getEntry("META-INF/scm/plugin.xml")

    if (entry != null) {
      String name = null
      String version = null

      XmlParser parser = new XmlParser()
      InputStream input = zip.getInputStream(entry)
      try {
        def pluginXml = parser.parse(input)
        name = pluginXml.information.name.text() as String
        version = pluginXml.conditions."min-version".text() as String
      } finally {
        input.close()
      }

      if (name.empty) {
        throw new GradleException("invalid dependency: ${smp}")
      }
      if (!version.empty) {
        return new Plugin(name, VersionNumber.parse(version))
      }
    }
    return null
  }

  private static class Plugin implements Comparable<Plugin> {

    private String name
    private VersionNumber version

    private Plugin(String name, VersionNumber version) {
      this.name = name
      this.version = version
    }

    @Override
    int compareTo(Plugin o) {
      return version.compareTo(o.version)
    }
  }
}
