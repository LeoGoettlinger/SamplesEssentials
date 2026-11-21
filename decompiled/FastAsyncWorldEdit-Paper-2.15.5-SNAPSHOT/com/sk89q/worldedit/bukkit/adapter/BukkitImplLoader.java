package com.sk89q.worldedit.bukkit.adapter;

import com.fastasyncworldedit.bukkit.util.MinecraftVersion;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.io.Closer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.logging.log4j.Logger;

public class BukkitImplLoader {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final List<String> adapterCandidates = new ArrayList<>();
   private final String minorMCVersion = String.valueOf(MinecraftVersion.getCurrent().getMinor());
   private int zeroth = 0;
   private String customCandidate;
   private static final String SEARCH_PACKAGE = "com.sk89q.worldedit.bukkit.adapter.impl.fawe";
   private static final String SEARCH_PACKAGE_DOT = "com.sk89q.worldedit.bukkit.adapter.impl.fawe.";
   private static final String SEARCH_PATH = "com.sk89q.worldedit.bukkit.adapter.impl.fawe".replace(".", "/");
   private static final String CLASS_SUFFIX = ".class";
   private static final String LOAD_ERROR_MESSAGE = "\n**********************************************\n** This FastAsyncWorldEdit version does not fully support your version of Bukkit.\n** You can fix this by:\n** - Updating your server version (Check /version to see how many versions you are behind)\n** - Updating FAWE\n**\n** When working with blocks or undoing, chests will be empty, signs\n** will be blank, and so on. There will be no support for entity\n** and block property-related functions.\n**********************************************\n";

   public BukkitImplLoader() {
      this.addDefaults();
   }

   private void addDefaults() {
      String className = System.getProperty("worldedit.bukkit.adapter");
      if (className != null) {
         this.customCandidate = className;
         this.zeroth = 1;
         this.adapterCandidates.add(className);
         LOGGER.info("-Dworldedit.bukkit.adapter used to add " + className + " to the list of available Bukkit adapters");
      }
   }

   public void addFromJar(File file) throws IOException {
      Closer closer = Closer.create();
      JarFile jar = closer.register(new JarFile(file));

      try {
         Enumeration<JarEntry> entries = jar.entries();

         while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String className = jarEntry.getName().replaceAll("[/\\\\]+", ".");
            if (className.startsWith("com.sk89q.worldedit.bukkit.adapter.impl.fawe.") && !jarEntry.isDirectory()) {
               int beginIndex = 0;
               int endIndex = className.length() - ".class".length();
               className = className.substring(beginIndex, endIndex);
               if (className.contains(this.minorMCVersion)) {
                  this.adapterCandidates.add(this.zeroth, className);
               } else {
                  this.adapterCandidates.add(className);
               }
            }
         }
      } finally {
         closer.close();
      }
   }

   public void addFromPath(ClassLoader classLoader) throws IOException {
      Enumeration<URL> resources = classLoader.getResources(SEARCH_PATH);

      while (resources.hasMoreElements()) {
         File file = new File(resources.nextElement().getFile());
         this.addFromPath(file);
      }
   }

   private void addFromPath(File file) {
      String resource = "com.sk89q.worldedit.bukkit.adapter.impl.fawe." + file.getName();
      if (file.isDirectory()) {
         File[] files = file.listFiles();
         if (files != null) {
            for (File child : files) {
               this.addFromPath(child);
            }
         }
      } else if (resource.endsWith(".class")) {
         int beginIndex = 0;
         int endIndex = resource.length() - ".class".length();
         String className = resource.substring(beginIndex, endIndex);
         if (className.contains(this.minorMCVersion)) {
            this.adapterCandidates.add(this.zeroth, className);
         } else {
            this.adapterCandidates.add(className);
         }
      }
   }

   public BukkitImplAdapter loadAdapter() throws AdapterLoadException {
      ClassLoader classLoader = this.getClass().getClassLoader();

      for (String className : this.adapterCandidates) {
         try {
            Class<?> cls = Class.forName(className, false, classLoader);
            if (!cls.isSynthetic() && BukkitImplAdapter.class.isAssignableFrom(cls)) {
               return (BukkitImplAdapter)cls.newInstance();
            }
         } catch (ClassNotFoundException var5) {
            LOGGER.warn("Failed to load the Bukkit adapter class '" + className + "' that is not supposed to be missing", var5);
         } catch (IllegalAccessException var6) {
            LOGGER.warn("Failed to load the Bukkit adapter class '" + className + "' that is not supposed to be raising this error", var6);
         } catch (Throwable var7) {
            if (className.equals(this.customCandidate)) {
               LOGGER.warn("Failed to load the Bukkit adapter class '" + className + "'", var7);
            }
         }
      }

      throw new AdapterLoadException(
         "\n**********************************************\n** This FastAsyncWorldEdit version does not fully support your version of Bukkit.\n** You can fix this by:\n** - Updating your server version (Check /version to see how many versions you are behind)\n** - Updating FAWE\n**\n** When working with blocks or undoing, chests will be empty, signs\n** will be blank, and so on. There will be no support for entity\n** and block property-related functions.\n**********************************************\n"
      );
   }
}
