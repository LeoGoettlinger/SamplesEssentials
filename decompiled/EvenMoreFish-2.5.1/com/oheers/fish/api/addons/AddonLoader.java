package com.oheers.fish.api.addons;

import com.oheers.fish.api.plugin.EMFPlugin;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

public abstract class AddonLoader {
   private final File addonFile;
   private final EMFPlugin plugin;
   private AddonMetadata cachedMetadata;

   protected AddonLoader(EMFPlugin plugin, File addonFile) {
      this.plugin = Objects.requireNonNull(plugin, "Plugin instance cannot be null");
      this.addonFile = addonFile;
   }

   public AddonMetadata getAddonMetadata() {
      if (this.cachedMetadata == null) {
         if (this.addonFile != null) {
            this.cachedMetadata = this.loadMetadataFromProperties(this.addonFile);
         }

         if (this.cachedMetadata == null) {
            this.cachedMetadata = this.loadAddonMetadata();
         }

         if (this.cachedMetadata == null) {
            throw new IllegalStateException("Could not load addon metadata - neither properties file exists nor metadata was provided");
         }
      }

      return this.cachedMetadata;
   }

   private AddonMetadata loadMetadataFromProperties(File addonFile) {
      try {
         AddonMetadata var13;
         try (JarFile jar = new JarFile(addonFile)) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
               this.plugin.getLogger().warning("No manifest found in addon: " + addonFile.getName());
               return null;
            }

            Attributes attributes = manifest.getMainAttributes();
            String name = attributes.getValue("name");
            String version = attributes.getValue("version");
            String authorsStr = attributes.getValue("authors");
            String website = attributes.getValue("website");
            String description = attributes.getValue("description");
            String dependenciesStr = attributes.getValue("dependencies");
            List<String> authors = authorsStr != null
               ? Arrays.stream(authorsStr.replaceAll("[\\[\\]]", "").split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()
               : Collections.emptyList();
            List<String> dependencies = dependenciesStr != null
               ? Arrays.stream(dependenciesStr.replaceAll("[\\[\\]]", "").split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()
               : Collections.emptyList();
            if (name == null || version == null || authors.isEmpty()) {
               this.plugin.getLogger().warning("MANIFEST.MF is missing required fields (name, version, or authors)");
               this.plugin.getLogger().warning(() -> "Name: %s, Version: %s, Authors: %s".formatted(name == null, version == null, authors.isEmpty()));
               return null;
            }

            var13 = new AddonMetadata(name, version, authors, description != null ? description : "", website, dependencies);
         }

         return var13;
      } catch (Exception var16) {
         this.plugin.getLogger().log(Level.WARNING, "Failed to load addon metadata from manifest file", (Throwable)var16);
         return null;
      }
   }

   protected AddonMetadata loadAddonMetadata() {
      return null;
   }

   public abstract void loadAddons();

   public final void load() {
      if (!this.canLoad()) {
         this.plugin.debug("Skipping loading of addon %s - canLoad() returned false".formatted(this.getAddonMetadata().name()));
      } else {
         try {
            this.validateMetadata();
            this.loadAddons();
            this.plugin
               .getLogger()
               .info("Loaded Addon %s %s by %s".formatted(this.getAddonMetadata().name(), this.getAddonMetadata().version(), this.getAddonMetadata().authors()));
         } catch (Exception var2) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to load addon %s".formatted(this.getAddonMetadata().name()), (Throwable)var2);
         }
      }
   }

   protected void validateMetadata() {
      AddonMetadata meta = this.getAddonMetadata();
      if (meta == null) {
         throw new IllegalStateException("Addon metadata cannot be null");
      } else if (meta.name().isBlank()) {
         throw new IllegalStateException("Addon name cannot be null or blank");
      }
   }

   public abstract boolean canLoad();

   public void unload() {
   }

   public void reload() {
      this.unload();
      this.load();
   }
}
