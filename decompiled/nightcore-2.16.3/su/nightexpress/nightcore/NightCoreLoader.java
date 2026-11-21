package su.nightexpress.nightcore;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository.Builder;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class NightCoreLoader implements PluginLoader {
   public void classloader(PluginClasspathBuilder classpathBuilder) {
      MavenLibraryResolver resolver = new MavenLibraryResolver();

      try {
         resolver.addRepository(new Builder("maven", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
      } catch (NoSuchFieldError var4) {
         resolver.addRepository(new Builder("maven", "default", "https://maven-central.storage-download.googleapis.com/maven2").build());
      }

      resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:6.3.2"), null));
      resolver.addDependency(new Dependency(new DefaultArtifact("it.unimi.dsi:fastutil-core:8.5.16"), null));
      classpathBuilder.addLibrary(resolver);
   }
}
