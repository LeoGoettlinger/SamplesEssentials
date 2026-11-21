package me.chancesd.pvpmanager.sdutils.library;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.yaml.snakeyaml.reader.UnicodeReader;

public class PluginLibraries {
   private PluginLibraries() {
   }

   public static boolean checkDependencies(Plugin plugin) {
      if (hasLibraries()) {
         return true;
      } else {
         AtomicBoolean isLoaded = new AtomicBoolean(true);
         Consumer<Exception> error = ex -> {
            Log.info(ex.getMessage());
            ex.printStackTrace();
            isLoaded.set(false);
         };
         Consumer<String> loaded = name -> Log.info("Library " + name + " loaded!");
         YamlConfiguration description = YamlConfiguration.loadConfiguration(new UnicodeReader(plugin.getResource("plugin.yml")));
         List<?> libraries = description.getList("libraries");
         if (libraries != null) {
            libraries.forEach(lib -> {
               String[] split = lib.toString().split(":");
               if (split.length == 3) {
                  new MavenCentralDependency(plugin, split[0], split[1], split[2]).load(loaded, error);
               }
            });
         }

         return isLoaded.get();
      }
   }

   public static void loadDependency(Plugin plugin, String group, String artifact, String version, String pattern, String relocated) {
      MavenCentralDependency dependency;
      if (pattern.isEmpty()) {
         dependency = new MavenCentralDependency(plugin, group, artifact, version);
      } else {
         dependency = new MavenCentralDependency(plugin, group, artifact, version).withRelocation(pattern, relocated);
      }

      dependency.load(name -> Log.info("Library " + name + " loaded!"), ex -> {
         Log.info(ex.getMessage());
         ex.printStackTrace();
      });
   }

   public static void loadDependency(Plugin plugin, String group, String artifact, String version) {
      loadDependency(plugin, group, artifact, version, "", "");
   }

   public static boolean hasLibraries() {
      try {
         PluginDescriptionFile.class.getMethod("getLibraries");
         if (MCVersion.isLowerThan(MCVersion.V1_21)) {
            try {
               Class.forName("com.mohistmc.MohistMCStart");
               return false;
            } catch (ClassNotFoundException var1) {
            }
         }

         return true;
      } catch (Exception var2) {
         return false;
      }
   }
}
