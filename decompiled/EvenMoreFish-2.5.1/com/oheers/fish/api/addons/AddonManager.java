package com.oheers.fish.api.addons;

import com.oheers.fish.api.FileUtil;
import com.oheers.fish.api.plugin.EMFPlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class AddonManager {
   private static final String ADDON_FOLDER = "addons";
   private final File folder;
   private final EMFPlugin plugin;
   private final List<AddonLoader> loadedAddons = new ArrayList<>();

   public AddonManager(EMFPlugin plugin) {
      this.plugin = plugin;
      this.folder = new File(plugin.getDataFolder(), "addons");
      FileUtil.deleteDirectory(this.folder);
      if (!this.folder.mkdirs()) {
         plugin.getLogger().warning("Could not create addons folder.");
      }
   }

   public void load() {
      List<File> jarFiles = FileUtil.getFilesInDirectoryWithExtension(this.folder, ".jar", true, true);
      jarFiles.forEach(this::processJar);
   }

   public void unload() {
      FileUtil.deleteDirectory(this.folder);
   }

   private void processJar(File jar) {
      List<Class<? extends AddonLoader>> classes = FileUtil.findClasses(jar, AddonLoader.class);
      if (classes.isEmpty()) {
         this.plugin.getLogger().warning(() -> "No AddonLoader classes found in %s".formatted(jar.getName()));
      } else {
         for (Class<? extends AddonLoader> clazz : classes) {
            this.loadAddonLoader(clazz, jar);
         }
      }
   }

   private void loadAddonLoader(Class<? extends AddonLoader> clazz, File addonFile) {
      try {
         AddonLoader loaderInstance = clazz.getDeclaredConstructor(EMFPlugin.class, File.class).newInstance(this.plugin, addonFile);
         loaderInstance.load();
         this.loadedAddons.add(loaderInstance);
      } catch (Exception var4) {
         this.plugin.getLogger().log(Level.WARNING, "Could not load addon %s:%s".formatted(clazz.getName(), var4.getMessage()), (Throwable)var4);
      }
   }

   public void unloadAll() {
      this.loadedAddons.forEach(AddonLoader::unload);
      this.loadedAddons.clear();
   }
}
