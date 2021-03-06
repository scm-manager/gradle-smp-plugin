package com.cloudogu.smp

import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.component.external.model.ivy.IvyModuleResolveMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject

@CacheableRule
abstract class SmpVariantRule implements ComponentMetadataRule {

  private static final Logger LOG = LoggerFactory.getLogger(SmpVariantRule)

  @Inject
  abstract ObjectFactory getObjects()

  @Override
  void execute(ComponentMetadataContext ctx) {
    def id = ctx.details.id
    if (isMissingMetadata(ctx)) {
      skip(id, 'missing metadata')
      return
    }
    if (isIvyResolvedDependency(ctx)) {
      skip(id, 'ivy metadata')
      return
    }

    if (isSmp(ctx)) {
      ctx.details.withVariant('runtime') {
        it.attributes {
          it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, 'smp'))
        }
        it.withDependencies {
          // Dependencies with a classifier point at JARs and can be removed
          // TODO needs public API - https://github.com/gradle/gradle/issues/11975
          it.removeAll { it.originalMetadata?.dependencyDescriptor?.dependencyArtifact?.classifier }
        }
      }
      ctx.details.withVariant('compile') {
        it.withFiles {
          it.removeAllFiles()
          it.addFile("${id.name}-${id.version}.jar")
        }
      }
      ctx.details.addVariant('jarRuntimeElements', 'runtime') {
        it.attributes {
          it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.JAR))
        }
        it.withFiles {
          it.removeAllFiles()
          it.addFile("${id.name}-${id.version}.jar")
        }
      }
    }
  }

  private boolean isMissingMetadata(ComponentMetadataContext ctx) {
    ctx.metadata == null
  }

  private boolean isIvyResolvedDependency(ComponentMetadataContext ctx) {
    ctx.metadata instanceof IvyModuleResolveMetadata
  }

  private boolean isSmp(ComponentMetadataContext ctx) {
    ctx.metadata.packaging == 'smp'
  }

  private static void skip(ModuleVersionIdentifier id, String reason) {
    LOG.debug('Skipping {} due to {}', id, reason)
  }

}
