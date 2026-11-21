package me.chancesd.pvpmanager.sdutils.library;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;
import me.chancesd.pvpmanager.sdutils.library.libby.BukkitLibraryManager;
import me.chancesd.pvpmanager.sdutils.library.libby.Library;
import me.chancesd.pvpmanager.sdutils.library.libby.relocation.Relocation;
import org.bukkit.plugin.Plugin;

public class MavenCentralDependency extends Dependency {
   private static final String ROOT_URL = "https://repo1.maven.org/maven2";
   private final String group;
   private final String artifact;
   private final String version;
   private Relocation relocation;

   MavenCentralDependency(Plugin plugin, String group, String artifact, String version) {
      super(plugin);
      this.group = group;
      this.artifact = artifact;
      this.version = version;
   }

   MavenCentralDependency withRelocation(String pattern, String relocatedPattern) {
      this.relocation = new Relocation(pattern, relocatedPattern);
      return this;
   }

   @Override
   void load(Consumer<String> onComplete, Consumer<Exception> onError) {
      try {
         BukkitLibraryManager libraryManager = new BukkitLibraryManager(this.plugin, "libraries");
         libraryManager.addMavenCentral();
         Library.Builder libBuilder = Library.builder().groupId(this.group).artifactId(this.artifact).version(this.version);
         if (this.relocation != null) {
            libBuilder.relocate(this.relocation);
         }

         libraryManager.loadLibrary(libBuilder.build());
         onComplete.accept(this.artifact);
      } catch (Exception var5) {
         onError.accept(var5);
      }
   }

   @Override
   protected URL buildUrl() throws MalformedURLException {
      String groupSlashed = String.join("/", this.group.split("\\."));
      String jarName = String.format("%s-%s.jar", this.artifact, this.version);
      return new URL(String.format("%s/%s/%s/%s/%s", "https://repo1.maven.org/maven2", groupSlashed, this.artifact, this.version, jarName));
   }

   @Override
   protected String getLocalName() {
      return String.format("%s-%s.jar", this.artifact, this.version);
   }
}
