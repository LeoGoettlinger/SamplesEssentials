package dev.cwhead.GravesX.util;

import com.ranull.graves.Graves;
import com.ranull.graves.bundledlibraries.libby.BukkitLibraryManager;
import com.ranull.graves.bundledlibraries.libby.Library;
import com.ranull.graves.bundledlibraries.libby.LibraryManager;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class LibraryLoaderUtil {
   private final Graves plugin;

   public LibraryLoaderUtil(Graves plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
   }

   public void loadLibrary(String groupID, String artifactID, String version) {
      this.loadLibrary(groupID, artifactID, version, null, null, null, false, null, true);
   }

   public void loadLibrary(String groupID, String artifactID, String version, boolean isIsolated) {
      this.loadLibrary(groupID, artifactID, version, null, null, null, isIsolated, null, true);
   }

   public void loadLibrary(String groupID, String artifactID, String version, String relocatePattern, String relocateRelocatedPattern, boolean isIsolated) {
      this.loadLibrary(groupID, artifactID, version, null, relocatePattern, relocateRelocatedPattern, isIsolated, null, true);
   }

   public void loadLibrary(
      String groupID,
      String artifactID,
      String version,
      String relocatePattern,
      String relocateRelocatedPattern,
      boolean isIsolated,
      boolean resolveTransitiveDependencies
   ) {
      this.loadLibrary(groupID, artifactID, version, null, relocatePattern, relocateRelocatedPattern, isIsolated, null, resolveTransitiveDependencies);
   }

   public void loadLibrary(
      String groupID, String artifactID, String version, String relocatePattern, String relocateRelocatedPattern, boolean isIsolated, String libraryURL
   ) {
      this.loadLibrary(groupID, artifactID, version, null, relocatePattern, relocateRelocatedPattern, isIsolated, libraryURL, true);
   }

   public void loadLibrary(
      String groupID,
      String artifactID,
      String version,
      String relocatePattern,
      String relocateRelocatedPattern,
      boolean isIsolated,
      String libraryURL,
      boolean resolveTransitiveDependencies
   ) {
      this.loadLibrary(groupID, artifactID, version, null, relocatePattern, relocateRelocatedPattern, isIsolated, libraryURL, resolveTransitiveDependencies);
   }

   public void loadLibrary(
      String groupID,
      String artifactID,
      String version,
      String ID,
      String relocatePattern,
      String relocateRelocatedPattern,
      boolean isIsolated,
      String libraryURL,
      boolean resolveTransitiveDependencies
   ) {
      if (groupID == null || groupID.isBlank()) {
         throw new IllegalArgumentException("groupID is required and cannot be blank.");
      } else if (artifactID == null || artifactID.isBlank()) {
         throw new IllegalArgumentException("artifactID is required and cannot be blank.");
      } else if (version != null && !version.isBlank()) {
         boolean doRelocate = isDoRelocate(relocatePattern, relocateRelocatedPattern);
         String groupPretty = groupID.replace("{}", ".");
         String libLabel = groupPretty + "." + artifactID + ":" + version;
         boolean hasId = ID != null && !ID.isBlank();
         String caseKey = (hasId ? "ID" : "PLAIN") + (doRelocate ? "+RELOC" : "");

         LibraryManager libraryManager;
         try {
            libraryManager = getLibraryManager(libraryURL, this.plugin);
         } catch (Exception var27) {
            this.plugin.getLogger().severe("Failed to construct LibraryManager for " + libLabel + ": " + var27.getClass().getSimpleName());
            this.plugin.logStackTrace(var27);
            return;
         }

         if (libraryManager == null) {
            this.plugin.getLogger().severe("LibraryManager was null for " + libLabel + " — aborting this library load.");
         } else {
            try {
               Library.Builder builder = Library.builder()
                  .groupId(groupID)
                  .artifactId(artifactID)
                  .version(version)
                  .resolveTransitiveDependencies(resolveTransitiveDependencies);
               switch (caseKey) {
                  case "ID+RELOC":
                     builder.loaderId(ID).relocate(relocatePattern, relocateRelocatedPattern);
                     break;
                  case "ID":
                     builder.loaderId(ID);
                     break;
                  case "PLAIN+RELOC":
                     builder.relocate(relocatePattern, relocateRelocatedPattern);
               }

               if (isIsolated) {
                  if (!hasId) {
                     builder.loaderId(artifactID + "-isolated");
                  }

                  builder.isolatedLoad(true);
               }

               Library lib;
               try {
                  lib = builder.build();
               } catch (Exception var26) {
                  this.plugin.getLogger().severe("Library.builder().build() failed for " + libLabel + ": " + var26.getClass().getSimpleName());
                  this.plugin.logStackTrace(var26);
                  return;
               }

               if (lib == null) {
                  this.plugin.getLogger().severe("Builder returned null Library for " + libLabel + " — skipping.");
                  return;
               }

               String loaderId = null;

               try {
                  loaderId = lib.getLoaderId();
               } catch (Throwable var25) {
                  this.plugin.getLogger().warning("Could not read loaderId for " + libLabel + ": " + var25.getClass().getSimpleName());
               }

               this.plugin
                  .debugMessage(
                     "Loading library "
                        + libLabel
                        + (isIsolated ? " [isolated]" : "")
                        + (doRelocate ? " [relocated]" : "")
                        + (!hasId && !isIsolated ? "" : " (loaderId=" + (loaderId == null ? "null" : loaderId) + ")"),
                     1
                  );

               try {
                  libraryManager.loadLibrary(lib);
               } catch (NullPointerException var23) {
                  this.plugin
                     .getLogger()
                     .severe(
                        "NullPointerException while loading library "
                           + libLabel
                           + ". Likely cause: missing repository, malformed library descriptor, or resolver returned null objects."
                     );
                  this.plugin.logStackTrace((Exception)var23);
                  return;
               } catch (Exception var24) {
                  this.plugin.getLogger().severe("Exception while loading library " + libLabel + ": " + var24.getClass().getSimpleName());
                  this.plugin.logStackTrace(var24);
                  return;
               }

               if (isIsolated && doRelocate) {
                  try {
                     if (relocateRelocatedPattern != null && !relocateRelocatedPattern.isBlank()) {
                        String probe = relocateRelocatedPattern.replace("{}", ".");
                        if (probe.indexOf(46) < 0) {
                           this.plugin
                              .getLogger()
                              .warning("Relocation verification probe '" + probe + "' looks invalid for " + libLabel + " — skipping verification.");
                        } else {
                           try {
                              Class.forName(probe, false, Thread.currentThread().getContextClassLoader());
                              this.plugin.debugMessage("Verified shaded library " + libLabel + ".", 1);
                           } catch (ClassNotFoundException var21) {
                              this.plugin.getLogger().severe("Shaded verification failed for " + libLabel + ": ClassNotFoundException");
                              this.plugin.logStackTrace((Exception)var21);
                              this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                           } catch (Throwable var22) {
                              this.plugin.getLogger().severe("Shaded verification failed for " + libLabel + ": " + var22.getClass().getSimpleName());
                              this.plugin.logStackTrace(var22);
                              this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                           }
                        }
                     } else {
                        this.plugin.getLogger().warning("Relocation target pattern is blank for " + libLabel + " — skipping verification.");
                     }
                  } catch (Throwable var28) {
                     this.plugin.getLogger().severe("Unexpected error during shaded verification for " + libLabel + ": " + var28.getClass().getSimpleName());
                     this.plugin.logStackTrace(var28);
                     this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                  }
               } else {
                  this.plugin.debugMessage("Loaded library " + libLabel + ".", 1);
               }
            } catch (IllegalArgumentException var29) {
               this.plugin.getLogger().severe("Invalid arguments while preparing to load " + libLabel + ": " + var29.getMessage());
               this.plugin.logStackTrace((Exception)var29);
            } catch (Exception var30) {
               this.plugin.getLogger().severe("Failed to load " + libLabel + ": " + var30.getClass().getSimpleName());
               this.plugin.logStackTrace(var30);
            }
         }
      } else {
         throw new IllegalArgumentException("version is required and cannot be blank.");
      }
   }

   @NotNull
   private static LibraryManager getLibraryManager(String libraryURL, Graves plugin) {
      LibraryManager libraryManager = new BukkitLibraryManager(plugin);
      if (libraryURL != null && !libraryURL.isBlank()) {
         try {
            libraryManager.addRepository(libraryURL);
         } catch (Exception var8) {
            plugin.getLogger().warning("Failed to add custom repository '" + libraryURL + "': " + var8.getClass().getSimpleName());
         }
      }

      try {
         libraryManager.addMavenCentral();
      } catch (Throwable var7) {
         plugin.getLogger().warning("addMavenCentral failed: " + var7.getClass().getSimpleName());
      }

      try {
         libraryManager.addSonatype();
      } catch (Throwable var6) {
         plugin.getLogger().warning("addSonatype failed: " + var6.getClass().getSimpleName());
      }

      try {
         libraryManager.addJCenter();
      } catch (Throwable var5) {
         Bukkit.getLogger().warning("addJCenter failed: " + var5.getClass().getSimpleName());
      }

      try {
         libraryManager.addJitPack();
      } catch (Throwable var4) {
         Bukkit.getLogger().warning("addJitPack failed: " + var4.getClass().getSimpleName());
      }

      return libraryManager;
   }

   private static boolean isDoRelocate(String relocatePattern, String relocateRelocatedPattern) {
      boolean hasRelocatePattern = relocatePattern != null && !relocatePattern.isBlank();
      boolean hasRelocateTarget = relocateRelocatedPattern != null && !relocateRelocatedPattern.isBlank();
      if (hasRelocatePattern ^ hasRelocateTarget) {
         throw new IllegalArgumentException("If relocation is used, both relocatePattern and relocateRelocatedPattern must be provided.");
      } else {
         return hasRelocatePattern;
      }
   }
}
