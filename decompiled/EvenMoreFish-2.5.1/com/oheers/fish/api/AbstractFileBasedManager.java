package com.oheers.fish.api;

import com.oheers.fish.api.plugin.EMFPlugin;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class AbstractFileBasedManager<T> {
   protected AbstractFileBasedManager<?> dependency;
   protected final TreeMap<String, T> itemMap;
   private boolean loaded = false;

   protected AbstractFileBasedManager() {
      this.itemMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      this.dependency = null;
      this.performPreLoadConversions();
   }

   protected AbstractFileBasedManager(AbstractFileBasedManager<?> dependency) {
      this.itemMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      this.dependency = dependency;
      this.performPreLoadConversions();
   }

   public void load() {
      if (!this.isLoaded()) {
         if (this.dependency != null && !this.dependency.isLoaded()) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " cannot load before " + this.dependency.getClass().getSimpleName());
         } else {
            this.loadItems();
            this.logLoadedItems();
            this.loaded = true;
         }
      }
   }

   public void reload() {
      if (this.isLoaded()) {
         this.clearMap(true);
         this.loadItems();
         this.logLoadedItems();
      }
   }

   public void unload() {
      if (this.isLoaded()) {
         this.clearMap(false);
         this.loaded = false;
      }
   }

   public boolean isLoaded() {
      return this.loaded;
   }

   @NonNull
   public TreeMap<String, T> getItemMap() {
      return this.itemMap;
   }

   @Nullable
   public T getItem(@Nullable String id) {
      return id != null && !id.isEmpty() ? this.itemMap.get(id) : null;
   }

   protected abstract void loadItems();

   protected abstract void logLoadedItems();

   protected abstract void performPreLoadConversions();

   protected void clearMap(boolean reload) {
      this.itemMap.clear();
   }

   protected <U> void loadItemsFromFiles(
      String folderName,
      AbstractFileBasedManager.FunctionWithException<File, U, InvalidConfigurationException> itemLoader,
      Function<U, String> idExtractor,
      Predicate<U> shouldSkipItem
   ) {
      File itemsFolder = new File(EMFPlugin.getInstance().getDataFolder(), folderName);
      if (!itemsFolder.exists()) {
         FileUtil.loadDefaultFiles(folderName, itemsFolder);
      }

      FileUtil.regenExampleFiles(folderName, itemsFolder);
      List<File> itemFiles = FileUtil.getFilesInDirectory(itemsFolder, true, true);
      if (itemFiles.isEmpty()) {
         EMFPlugin.getInstance().debug("No files found in directory: " + itemsFolder);
      } else {
         itemFiles.stream().map(file -> {
            try {
               return itemLoader.apply(file);
            } catch (InvalidConfigurationException var3x) {
               Logging.warn("Invalid configuration in file: " + file.getName());
               Logging.warn(var3x.getMessage());
               return null;
            }
         }).filter(Objects::nonNull).filter(item -> !shouldSkipItem.test((U)item)).forEach(item -> {
            String id = idExtractor.apply((U)item);
            if (this.itemMap.containsKey(id)) {
               Logging.warn("An item with the ID '" + id + "' already exists! Skipping.");
            } else {
               this.itemMap.put(id, (T)item);
            }
         });
      }
   }

   @FunctionalInterface
   public interface FunctionWithException<T, R, E extends Exception> {
      R apply(T var1) throws E;
   }
}
