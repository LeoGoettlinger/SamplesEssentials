package com.oheers.fish.plugin.loading;

import com.oheers.fish.api.plugin.EMFPlugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;

public class EMFVersionLoader {
   private final EMFPlugin plugin;
   private final URLClassLoader jar;
   private final EMFVersionProvider version;

   public EMFVersionLoader(@NonNull EMFPlugin plugin, @NonNull ClassLoader parent) {
      this.plugin = plugin;
      this.jar = this.getClassLoader(parent);
      this.version = this.fetchVersion();
   }

   @NonNull
   public EMFVersionProvider getVersionProvider() {
      return this.version;
   }

   public void onDisable() {
      try {
         this.jar.close();
      } catch (IOException var2) {
         throw new RuntimeException(var2);
      }
   }

   private EMFVersionProvider fetchVersion() {
      try {
         Class<?> clazz = this.jar.loadClass("org.evenmorefish.fish.EMFVersion");
         return (EMFVersionProvider)clazz.getDeclaredConstructor(EMFPlugin.class).newInstance(this.plugin);
      } catch (Exception var2) {
         throw new RuntimeException("Failed to load EvenMoreFish", var2);
      }
   }

   private URLClassLoader getClassLoader(ClassLoader parent) {
      try {
         URLClassLoader var4;
         try (InputStream is = this.getURL(parent).openStream()) {
            this.plugin.getDataFolder().mkdirs();
            File file = new File(this.plugin.getDataFolder(), Bukkit.getMinecraftVersion() + ".jar");
            file.deleteOnExit();
            Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            var4 = new URLClassLoader(new URL[]{file.toURI().toURL()}, parent);
         }

         return var4;
      } catch (IOException var7) {
         throw new RuntimeException("Failed to load EvenMoreFish", var7);
      }
   }

   private URL getURL(ClassLoader classLoader) {
      String version = Bukkit.getMinecraftVersion();
      if (version.startsWith("26.3")) {
         return classLoader.getResource("versions/26-3.jar");
      } else if (version.startsWith("26.2")) {
         return classLoader.getResource("versions/26-2.jar");
      } else if (version.startsWith("26.1")) {
         return classLoader.getResource("versions/26-1.jar");
      } else if (version.startsWith("1.21")) {
         return switch (version) {
            case "1.21" -> throw new IllegalStateException("EvenMoreFish does not support this Minecraft version.");
            case "1.21.1", "1.21.3", "1.21.4" -> classLoader.getResource("versions/1.21.1-4.jar");
            default -> classLoader.getResource("versions/1.21.5-11.jar");
         };
      } else {
         throw new IllegalStateException("EvenMoreFish does not support this Minecraft version.");
      }
   }
}
