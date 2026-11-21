package dev.cwhead.GravesX.module.util;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.module.ModuleContext;
import dev.cwhead.GravesX.util.LibraryLoaderUtil;
import java.util.Locale;
import java.util.Objects;

public final class LibbyImporter implements LibraryImporter {
   private final Graves plugin;
   private final LibraryLoaderUtil util;

   public LibbyImporter(Graves plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.util = new LibraryLoaderUtil(plugin);
   }

   @Override
   public void importLibrary(ModuleContext ctx, String... coordinates) {
      if (coordinates != null) {
         for (String spec : coordinates) {
            if (spec != null) {
               String s = spec.trim();
               if (!s.isEmpty()) {
                  this.loadOneFromStringSpec(s);
               }
            }
         }
      }
   }

   @Override
   public void importLibrary(ModuleContext ctx, ModuleInfo.LibraryDef def) {
      if (def != null) {
         String coordsRaw = def.coordinates();
         if (coordsRaw != null && !coordsRaw.isBlank()) {
            if (coordsRaw.indexOf(63) >= 0) {
               this.loadOneFromStringSpec(coordsRaw.trim());
            } else {
               String[] parts = coordsRaw.trim().split(":");
               if (parts.length < 3) {
                  this.plugin.getLogger().severe("Failed to parse library coordinates: " + coordsRaw + " (expected group:artifact:version)");
                  this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
               } else {
                  String group = parts[0].trim();
                  String artifact = parts[1].trim();
                  String version = parts[2].trim();
                  boolean isolated = def.isIsolated() != null && def.isIsolated();
                  boolean resolveTransitive = def.useTransitive() == null || def.useTransitive();
                  String relocateFrom = nv(def.relocateFrom());
                  String relocateTo = nv(def.relocateTo());
                  String repo = nv(def.repo());
                  String id = nv(def.id());

                  try {
                     if (id != null || repo != null) {
                        this.util.loadLibrary(group, artifact, version, id, relocateFrom, relocateTo, isolated, repo, resolveTransitive);
                        return;
                     }

                     if (relocateFrom != null || relocateTo != null) {
                        this.util.loadLibrary(group, artifact, version, relocateFrom, relocateTo, isolated, resolveTransitive);
                        return;
                     }

                     if (isolated) {
                        this.util.loadLibrary(group, artifact, version, true);
                        return;
                     }

                     this.util.loadLibrary(group, artifact, version);
                  } catch (Exception var15) {
                     this.plugin.getLogger().severe("Failed to load library: " + coordsRaw + " -> " + var15.getMessage());
                     this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                  }
               }
            }
         }
      }
   }

   private void loadOneFromStringSpec(String spec) {
      try {
         String coords = spec;
         String query = null;
         int q = spec.indexOf(63);
         if (q >= 0) {
            coords = spec.substring(0, q);
            query = spec.substring(q + 1);
         }

         String[] parts = coords.split(":");
         if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid coordinates: " + spec);
         }

         String group = parts[0];
         String artifact = parts[1];
         String version = parts[2];
         boolean isolated = false;
         boolean resolveTransitive = true;
         String relocateFrom = null;
         String relocateTo = null;
         String repo = null;
         String id = null;
         if (query != null) {
            String[] kvps = query.split("&");

            for (String kvp : kvps) {
               int eq = kvp.indexOf(61);
               String key = (eq >= 0 ? kvp.substring(0, eq) : kvp).trim().toLowerCase(Locale.ROOT);
               String value = (eq >= 0 ? kvp.substring(eq + 1) : "").trim();
               if ("isolated".equals(key)) {
                  isolated = parseBoolean(value);
               } else if ("transitive".equals(key)) {
                  resolveTransitive = parseBoolean(value);
               } else if ("repo".equals(key)) {
                  repo = value;
               } else if ("id".equals(key)) {
                  id = value;
               } else if ("relocate".equals(key)) {
                  int arrow = value.indexOf("->");
                  if (arrow > 0) {
                     relocateFrom = value.substring(0, arrow).trim();
                     relocateTo = value.substring(arrow + 2).trim();
                  }
               } else if ("relocatefrom".equals(key)) {
                  relocateFrom = value;
               } else if ("relocateto".equals(key)) {
                  relocateTo = value;
               }
            }
         }

         if (id != null || repo != null) {
            this.util.loadLibrary(group, artifact, version, id, relocateFrom, relocateTo, isolated, repo, resolveTransitive);
         } else if (relocateFrom != null || relocateTo != null) {
            this.util.loadLibrary(group, artifact, version, relocateFrom, relocateTo, isolated, resolveTransitive);
         } else if (isolated) {
            this.util.loadLibrary(group, artifact, version, true);
         } else {
            this.util.loadLibrary(group, artifact, version);
         }
      } catch (Exception var24) {
         this.plugin.getLogger().severe("Failed to parse or load library spec: " + spec + " -> " + var24.getMessage());
         this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
      }
   }

   private static boolean parseBoolean(String s) {
      String v = (s == null ? "" : s).toLowerCase(Locale.ROOT);
      return "1".equals(v) || "true".equals(v) || "yes".equals(v) || "y".equals(v);
   }

   private static String nv(String s) {
      return s != null && !s.isBlank() ? s.trim() : null;
   }
}
