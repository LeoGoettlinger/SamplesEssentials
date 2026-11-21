package dev.cwhead.GravesX.addon;

import com.ranull.graves.Graves;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class GravesXAddon {
   public static void ensureAddonRoot(Graves plugin) {
      File data = plugin.getDataFolder();
      File addonRoot = new File(data, "addon");

      try {
         Files.createDirectories(addonRoot.toPath());
      } catch (AccessDeniedException var4) {
         plugin.getLogger()
            .log(
               Level.SEVERE,
               "GravesX does not have permission to create or access the addon folder:\n  "
                  + addonRoot.getAbsolutePath()
                  + "\nFix: ensure the server process user has read/write access to:\n  "
                  + data.getAbsolutePath()
                  + "\nIf you're on Linux, you may need to adjust ownership/permissions (chown/chmod).\nIf you're on Windows, ensure the account running the server has Modify permission.",
               (Throwable)var4
            );
         plugin.logStackTrace((Exception)var4);
      } catch (IOException var5) {
         plugin.getLogger().log(Level.SEVERE, "Failed to create addon folder: " + addonRoot.getAbsolutePath(), (Throwable)var5);
         plugin.logStackTrace((Exception)var5);
      }
   }

   public static File ensureAddonFolder(Graves plugin, String addonName) {
      Objects.requireNonNull(plugin, "plugin");
      if (addonName != null && !addonName.trim().isEmpty()) {
         File data = plugin.getDataFolder();
         File addonRoot = new File(data, "addon");
         File addonDir = new File(addonRoot, sanitize(addonName));

         try {
            Files.createDirectories(addonDir.toPath());
            return addonDir;
         } catch (IOException var6) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create addon folder: " + addonDir.getAbsolutePath(), (Throwable)var6);
            throw new IllegalStateException("Could not create addon folder: " + addonDir, var6);
         }
      } else {
         throw new IllegalArgumentException("addonName cannot be null or blank");
      }
   }

   public static int exportAddonConfigs(Graves plugin, String addonName, boolean replaceIfExists) {
      Objects.requireNonNull(plugin, "plugin");
      Objects.requireNonNull(addonName, "addonName");
      File destDir = ensureAddonFolder(plugin, addonName);
      Path destAbs = destDir.toPath().toAbsolutePath().normalize();
      String resourcePrefix = "addon/" + addonName + "/";
      int written = 0;

      try {
         URL codeSource = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
         File codeFile = new File(codeSource.toURI());
         if (codeFile.isFile() && codeFile.getName().endsWith(".jar")) {
            try (JarFile jar = new JarFile(codeFile)) {
               Enumeration<JarEntry> en = jar.entries();

               while (en.hasMoreElements()) {
                  JarEntry entry = en.nextElement();
                  String name = entry.getName();
                  if (!entry.isDirectory() && name.startsWith(resourcePrefix) && name.endsWith(".yml")) {
                     String fileName = name.substring(resourcePrefix.length());
                     if (!fileName.contains("/")) {
                        Path target = destAbs.resolve(fileName).normalize();
                        if (!target.startsWith(destAbs)) {
                           plugin.getLogger().warning("Skipping suspicious path in JAR: " + name);
                        } else if (!Files.exists(target) || replaceIfExists) {
                           Path parent = target.getParent();
                           if (parent != null) {
                              Files.createDirectories(parent);
                           }

                           try (InputStream in = jar.getInputStream(entry)) {
                              Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                           }

                           if (Files.isSymbolicLink(target)) {
                              Files.delete(target);
                              plugin.getLogger().warning("Skipped symlink file: " + name);
                           } else {
                              written++;
                           }
                        }
                     }
                  }
               }
            }
         } else if (codeFile.isDirectory()) {
            File resFolder = new File(codeFile, resourcePrefix);
            if (resFolder.isDirectory()) {
               File[] files = resFolder.listFiles((dir, n) -> n.endsWith(".yml"));
               if (files != null) {
                  for (File f : files) {
                     Path target = destAbs.resolve(f.getName()).normalize();
                     if (!target.startsWith(destAbs)) {
                        plugin.getLogger().warning("Skipping suspicious dev file: " + f.getPath());
                     } else if (!Files.exists(target) || replaceIfExists) {
                        Path parentx = target.getParent();
                        if (parentx != null) {
                           Files.createDirectories(parentx);
                        }

                        Files.copy(f.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                        written++;
                     }
                  }
               }
            } else {
               plugin.getLogger().warning("Resource path not found in dev environment: " + resFolder.getPath());
            }
         } else {
            plugin.getLogger().warning("Unknown plugin code source type: " + codeFile);
         }
      } catch (IOException | URISyntaxException var23) {
         plugin.getLogger().log(Level.SEVERE, "Failed exporting addon configs for " + addonName, (Throwable)var23);
         throw new IllegalStateException("Export failed for addon: " + addonName, var23);
      }

      if (written > 0) {
         plugin.debugMessage("Exported " + written + " config file(s) for addon '" + addonName + "'.", 1);
      } else {
         plugin.debugMessage("No new configs exported for addon '" + addonName + "'.", 1);
      }

      return written;
   }

   private static boolean copyResourceIfNeeded(Graves plugin, String resourcePath, File target, boolean replace) throws IOException {
      if (target.exists() && !replace) {
         return false;
      } else {
         boolean var5;
         try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
               plugin.getLogger().warning("Missing bundled resource: " + resourcePath);
               return false;
            }

            Files.createDirectories(target.getParentFile().toPath());
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            var5 = true;
         }

         return var5;
      }
   }

   private static String sanitize(String s) {
      return s.replaceAll("[^A-Za-z0-9._-]", "_");
   }
}
