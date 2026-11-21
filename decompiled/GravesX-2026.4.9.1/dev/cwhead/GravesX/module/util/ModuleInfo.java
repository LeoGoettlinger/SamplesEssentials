package dev.cwhead.GravesX.module.util;

import dev.cwhead.GravesX.module.GravesXModuleController;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class ModuleInfo {
   private final String name;
   private final String description;
   private final String mainClass;
   private final String version;
   private final String website;
   private final List<String> authors;
   private final List<String> pluginDepends;
   private final List<String> pluginSoftDepends;
   private final List<String> pluginLoadBefore;
   private final List<String> moduleDepends;
   private final List<String> moduleSoftDepends;
   private final List<String> moduleLoadBefore;
   private final boolean supportsFolia;
   private final GravesXModuleController.LoadPhase loadPhase;
   private final Map<String, ModuleInfo.PermissionDef> permissions;
   private final Map<String, ModuleInfo.CommandDef> commands;
   private final List<ModuleInfo.LibraryDef> libraries;

   private ModuleInfo(
      String name,
      String description,
      String mainClass,
      String version,
      String website,
      List<String> authors,
      List<String> pDep,
      List<String> pSoft,
      List<String> pBefore,
      List<String> mDep,
      List<String> mSoft,
      List<String> mBefore,
      boolean supportsFolia,
      GravesXModuleController.LoadPhase loadPhase,
      Map<String, ModuleInfo.PermissionDef> permissions,
      Map<String, ModuleInfo.CommandDef> commands,
      List<ModuleInfo.LibraryDef> libraries
   ) {
      this.name = name;
      this.description = nv(description);
      this.mainClass = mainClass;
      this.version = version != null && !version.isBlank() ? version : "0.0.0";
      this.website = nv(website);
      this.authors = List.copyOf(authors == null ? List.of() : authors);
      this.pluginDepends = List.copyOf(pDep);
      this.pluginSoftDepends = List.copyOf(pSoft);
      this.pluginLoadBefore = List.copyOf(pBefore);
      this.moduleDepends = List.copyOf(mDep);
      this.moduleSoftDepends = List.copyOf(mSoft);
      this.moduleLoadBefore = List.copyOf(mBefore);
      this.supportsFolia = supportsFolia;
      this.loadPhase = loadPhase != null ? loadPhase : GravesXModuleController.LoadPhase.COMPLETED;
      this.permissions = Map.copyOf(permissions == null ? Map.of() : permissions);
      this.commands = Map.copyOf(commands == null ? Map.of() : commands);
      this.libraries = List.copyOf(libraries == null ? List.of() : libraries);
   }

   public String name() {
      return this.name;
   }

   public String description() {
      return this.description;
   }

   public String mainClass() {
      return this.mainClass;
   }

   public String version() {
      return this.version;
   }

   public String website() {
      return this.website;
   }

   public List<String> authors() {
      return this.authors;
   }

   public List<String> pluginDepends() {
      return this.pluginDepends;
   }

   public List<String> pluginSoftDepends() {
      return this.pluginSoftDepends;
   }

   public List<String> pluginLoadBefore() {
      return this.pluginLoadBefore;
   }

   public List<String> moduleDepends() {
      return this.moduleDepends;
   }

   public List<String> moduleSoftDepends() {
      return this.moduleSoftDepends;
   }

   public List<String> moduleLoadBefore() {
      return this.moduleLoadBefore;
   }

   public boolean supportsFolia() {
      return this.supportsFolia;
   }

   public GravesXModuleController.LoadPhase loadPhase() {
      return this.loadPhase;
   }

   public List<ModuleInfo.LibraryDef> libraries() {
      return this.libraries;
   }

   public Map<String, ModuleInfo.PermissionDef> permissions() {
      return this.permissions;
   }

   public Map<String, ModuleInfo.CommandDef> commands() {
      return this.commands;
   }

   public static ModuleInfo fromYaml(InputStream in) throws Exception {
      String text = new String(in.readAllBytes());
      Map<String, List<String>> lists = new HashMap<>();
      Map<String, String> scalars = new HashMap<>();
      Map<String, ModuleInfo.PermissionDef> permissions = new LinkedHashMap<>();
      Map<String, ModuleInfo.CommandDef> commands = new LinkedHashMap<>();
      List<ModuleInfo.LibraryDef> libraries = new ArrayList<>();
      String currentList = null;
      String currentSection = null;

      class PermBuilder {
         String node;
         String description;
         String defaultValue;
      }

      PermBuilder currentPerm = null;

      class CmdBuilder {
         String name;
         String description;
         String usage;
         String permission;
         String executor;
         String tabCompleter;
         List<String> aliases = new ArrayList<>();
      }

      CmdBuilder currentCmd = null;

      class LibBuilder {
         String coordinates;
         String relocateFrom;
         String relocateTo;
         Boolean isIsolated;
         Boolean useTransitive;
         String repo;
         String id;
      }

      LibBuilder currentLib = null;
      Pattern keyLine = Pattern.compile("^[A-Za-z][A-Za-z0-9_-]*\\s*:");
      Set<String> listKeys = new HashSet<>(
         Set.of(
            "authors", "plugindepends", "pluginsoftdepends", "pluginloadbefore", "moduledepends", "modulesoftdepends", "modulesoftdepend", "moduleloadbefore"
         )
      );
      String[] lines = text.split("\\R");

      for (String raw : lines) {
         String trimmed = raw.trim();
         if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
            int indent = leadingSpaces(raw);
            if (indent == 0 && keyLine.matcher(trimmed).find()) {
               String topKeyName = trimmed.split(":")[0].trim();
               if (!"permissions".equalsIgnoreCase(topKeyName) && currentPerm != null && currentPerm.node != null) {
                  permissions.put(currentPerm.node, new ModuleInfo.PermissionDef(currentPerm.node, nv(currentPerm.description), nv(currentPerm.defaultValue)));
                  currentPerm = null;
               }

               if (!"commands".equalsIgnoreCase(topKeyName) && currentCmd != null && currentCmd.name != null) {
                  commands.put(
                     currentCmd.name,
                     new ModuleInfo.CommandDef(
                        currentCmd.name,
                        nv(currentCmd.description),
                        nv(currentCmd.usage),
                        nv(currentCmd.permission),
                        currentCmd.aliases,
                        nv(currentCmd.executor),
                        nv(currentCmd.tabCompleter)
                     )
                  );
                  currentCmd = null;
               }

               if (!"libraries".equalsIgnoreCase(topKeyName) && currentLib != null && currentLib.coordinates != null) {
                  libraries.add(
                     new ModuleInfo.LibraryDef(
                        nv(currentLib.coordinates),
                        nv(currentLib.relocateFrom),
                        nv(currentLib.relocateTo),
                        currentLib.isIsolated,
                        currentLib.useTransitive,
                        nv(currentLib.repo),
                        nv(currentLib.id)
                     )
                  );
                  currentLib = null;
               }

               int idx = trimmed.indexOf(58);
               String key = trimmed.substring(0, idx).trim();
               String val = trimmed.substring(idx + 1).trim();
               String k = key.toLowerCase(Locale.ROOT);
               currentList = null;
               currentSection = null;
               if ("permissions".equals(k)) {
                  currentSection = "permissions";
               } else if ("commands".equals(k)) {
                  currentSection = "commands";
               } else if ("libraries".equals(k)) {
                  currentSection = "libraries";
               } else {
                  boolean isListKey = listKeys.contains(k);
                  if (val.isEmpty()) {
                     if (isListKey) {
                        lists.computeIfAbsent(k, __ -> new ArrayList<>());
                        currentList = k;
                     } else {
                        scalars.put(k, "");
                     }
                  } else if (!isListKey) {
                     if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                        val = val.substring(1, val.length() - 1);
                     }

                     scalars.put(k, val);
                  } else {
                     List<String> arr = new ArrayList<>();
                     String[] parts = val.split(",");

                     for (String p : parts) {
                        String v = p.trim();
                        if (!v.isEmpty()) {
                           arr.add(v);
                        }
                     }

                     lists.put(k, arr);
                  }
               }
            } else if (trimmed.startsWith("-") && currentList != null && currentSection == null) {
               String v = trimmed.substring(1).trim();
               if (!v.isEmpty()) {
                  lists.get(currentList).add(v);
               }
            } else if ("libraries".equals(currentSection)) {
               if (indent == 2 && trimmed.startsWith("-")) {
                  if (currentLib != null && currentLib.coordinates != null) {
                     libraries.add(
                        new ModuleInfo.LibraryDef(
                           nv(currentLib.coordinates),
                           nv(currentLib.relocateFrom),
                           nv(currentLib.relocateTo),
                           currentLib.isIsolated,
                           currentLib.useTransitive,
                           nv(currentLib.repo),
                           nv(currentLib.id)
                        )
                     );
                  }

                  currentLib = new LibBuilder();
                  currentLib.coordinates = stripSimpleQuotes(trimmed.substring(1).trim());
               } else {
                  int idx = trimmed.indexOf(58);
                  if (idx > 0 && currentLib != null) {
                     String key = trimmed.substring(0, idx).trim();
                     String val = stripSimpleQuotes(trimmed.substring(idx + 1).trim());
                     String lk = key.toLowerCase(Locale.ROOT);
                     if ("relocatefrom".equals(lk)) {
                        currentLib.relocateFrom = val;
                     } else if ("relocateto".equals(lk) || "relocateto".equals(lk)) {
                        currentLib.relocateTo = val;
                     } else if ("isisolated".equals(lk)) {
                        currentLib.isIsolated = parseBoolNullable(val);
                     } else if ("usetransitive".equals(lk)) {
                        currentLib.useTransitive = parseBoolNullable(val);
                     } else if ("repo".equals(lk)) {
                        currentLib.repo = val;
                     } else if ("id".equals(lk)) {
                        currentLib.id = val;
                     }
                  }
               }
            } else if ("permissions".equals(currentSection)) {
               int idx = trimmed.indexOf(58);
               if (idx > 0) {
                  String key = trimmed.substring(0, idx).trim();
                  String val = trimmed.substring(idx + 1).trim();
                  if (indent == 2) {
                     if (currentPerm != null && currentPerm.node != null) {
                        permissions.put(
                           currentPerm.node, new ModuleInfo.PermissionDef(currentPerm.node, nv(currentPerm.description), nv(currentPerm.defaultValue))
                        );
                     }

                     currentPerm = new PermBuilder();
                     currentPerm.node = key;
                  } else if (indent >= 4 && currentPerm != null) {
                     if ("description".equalsIgnoreCase(key)) {
                        currentPerm.description = stripSimpleQuotes(val);
                     } else if ("default".equalsIgnoreCase(key)) {
                        currentPerm.defaultValue = stripSimpleQuotes(val);
                     }
                  }
               }
            } else if ("commands".equals(currentSection)) {
               int idx = trimmed.indexOf(58);
               if (idx > 0) {
                  String key = trimmed.substring(0, idx).trim();
                  String val = trimmed.substring(idx + 1).trim();
                  if (indent == 2) {
                     if (currentCmd != null && currentCmd.name != null) {
                        commands.put(
                           currentCmd.name,
                           new ModuleInfo.CommandDef(
                              currentCmd.name,
                              nv(currentCmd.description),
                              nv(currentCmd.usage),
                              nv(currentCmd.permission),
                              currentCmd.aliases,
                              nv(currentCmd.executor),
                              nv(currentCmd.tabCompleter)
                           )
                        );
                     }

                     currentCmd = new CmdBuilder();
                     currentCmd.name = key;
                  } else if (indent >= 4 && currentCmd != null) {
                     String valStripped = stripSimpleQuotes(val);
                     if ("description".equalsIgnoreCase(key)) {
                        currentCmd.description = valStripped;
                     } else if ("usage".equalsIgnoreCase(key)) {
                        currentCmd.usage = valStripped;
                     } else if ("permission".equalsIgnoreCase(key)) {
                        currentCmd.permission = valStripped;
                     } else if ("aliases".equalsIgnoreCase(key)) {
                        currentCmd.aliases = parseInlineList(valStripped);
                     } else if ("executor".equalsIgnoreCase(key)) {
                        currentCmd.executor = valStripped;
                     } else if ("tab-completer".equalsIgnoreCase(key) || "tabcompleter".equalsIgnoreCase(key)) {
                        currentCmd.tabCompleter = valStripped;
                     }
                  }
               }
            }
         }
      }

      if (currentPerm != null && currentPerm.node != null) {
         permissions.put(currentPerm.node, new ModuleInfo.PermissionDef(currentPerm.node, nv(currentPerm.description), nv(currentPerm.defaultValue)));
      }

      if (currentCmd != null && currentCmd.name != null) {
         commands.put(
            currentCmd.name,
            new ModuleInfo.CommandDef(
               currentCmd.name,
               nv(currentCmd.description),
               nv(currentCmd.usage),
               nv(currentCmd.permission),
               currentCmd.aliases,
               nv(currentCmd.executor),
               nv(currentCmd.tabCompleter)
            )
         );
      }

      if (currentLib != null && currentLib.coordinates != null) {
         libraries.add(
            new ModuleInfo.LibraryDef(
               nv(currentLib.coordinates),
               nv(currentLib.relocateFrom),
               nv(currentLib.relocateTo),
               currentLib.isIsolated,
               currentLib.useTransitive,
               nv(currentLib.repo),
               nv(currentLib.id)
            )
         );
      }

      String name = nv(scalars.get("name"));
      String desc = nv(scalars.get("description"));
      String main = nv(scalars.get("main"));
      String ver = scalars.getOrDefault("version", "0.0.0");
      String site = nv(scalars.get("website"));
      List<String> authors = new ArrayList<>(lists.getOrDefault("authors", List.of()));
      String singleAuthor = nv(scalars.get("author"));
      if (singleAuthor != null && !singleAuthor.isEmpty()) {
         authors.add(singleAuthor);
      }

      List<String> cleanAuthors = new ArrayList<>();

      for (String a : authors) {
         String t = a == null ? null : a.trim();
         if (t != null && !t.isEmpty()) {
            cleanAuthors.add(t);
         }
      }

      boolean supportsFolia = Boolean.parseBoolean(scalars.getOrDefault("supportsfolia", "false"));
      GravesXModuleController.LoadPhase loadPhase = parseLoadPhase(scalars.get("load"));
      List<String> moduleSoftDeps = new ArrayList<>(lists.getOrDefault("modulesoftdepends", List.of()));
      moduleSoftDeps.addAll(lists.getOrDefault("modulesoftdepend", List.of()));
      return new ModuleInfo(
         name,
         desc,
         main,
         ver,
         site,
         cleanAuthors,
         lists.getOrDefault("plugindepends", List.of()),
         lists.getOrDefault("pluginsoftdepends", List.of()),
         lists.getOrDefault("pluginloadbefore", List.of()),
         lists.getOrDefault("moduledepends", List.of()),
         moduleSoftDeps,
         lists.getOrDefault("moduleloadbefore", List.of()),
         supportsFolia,
         loadPhase,
         permissions,
         commands,
         libraries
      );
   }

   private static GravesXModuleController.LoadPhase parseLoadPhase(String raw) {
      String v = nv(stripSimpleQuotes(raw));
      if (v == null) {
         return GravesXModuleController.LoadPhase.COMPLETED;
      } else {
         String u = v.trim().toUpperCase(Locale.ROOT);

         try {
            return GravesXModuleController.LoadPhase.valueOf(u);
         } catch (IllegalArgumentException var4) {
            return GravesXModuleController.LoadPhase.COMPLETED;
         }
      }
   }

   private static String nv(String s) {
      return s != null && !s.isBlank() ? s : null;
   }

   private static int leadingSpaces(String s) {
      int count = 0;

      while (count < s.length()) {
         char c = s.charAt(count);
         if (c == ' ') {
            count++;
         } else {
            if (c != '\t') {
               break;
            }

            count += 4;
         }
      }

      return count;
   }

   private static String stripSimpleQuotes(String s) {
      if (s == null) {
         return null;
      } else {
         String t = s.trim();
         if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
            t = t.substring(1, t.length() - 1);
         }

         if (t.startsWith("'") && t.endsWith("'") && t.length() >= 2) {
            t = t.substring(1, t.length() - 1);
         }

         return t;
      }
   }

   private static List<String> parseInlineList(String s) {
      List<String> out = new ArrayList<>();
      if (s == null) {
         return out;
      } else {
         String t = s.trim();
         if (t.startsWith("[") && t.endsWith("]")) {
            String inner = t.substring(1, t.length() - 1).trim();
            if (inner.isEmpty()) {
               return out;
            } else {
               String[] parts = inner.split(",");

               for (String p : parts) {
                  String v = stripSimpleQuotes(p.trim());
                  if (v != null && !v.isEmpty()) {
                     out.add(v);
                  }
               }

               return out;
            }
         } else {
            if (!t.isEmpty()) {
               out.add(stripSimpleQuotes(t));
            }

            return out;
         }
      }
   }

   private static Boolean parseBoolNullable(String s) {
      if (s == null) {
         return null;
      } else {
         String t = s.trim();
         if (t.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
         } else {
            return t.equalsIgnoreCase("false") ? Boolean.FALSE : null;
         }
      }
   }

   public static final class CommandDef {
      private final String name;
      private final String description;
      private final String usage;
      private final String permission;
      private final List<String> aliases;
      private final String executor;
      private final String tabCompleter;

      public CommandDef(String name, String description, String usage, String permission, List<String> aliases, String executor, String tabCompleter) {
         this.name = name;
         this.description = description;
         this.usage = usage;
         this.permission = permission;
         this.aliases = List.copyOf(aliases == null ? List.of() : aliases);
         this.executor = executor;
         this.tabCompleter = tabCompleter;
      }

      public String name() {
         return this.name;
      }

      public String description() {
         return this.description;
      }

      public String usage() {
         return this.usage;
      }

      public String permission() {
         return this.permission;
      }

      public List<String> aliases() {
         return this.aliases;
      }

      public String executor() {
         return this.executor;
      }

      public String tabCompleter() {
         return this.tabCompleter;
      }
   }

   public static final class LibraryDef {
      private final String coordinates;
      private final String relocateFrom;
      private final String relocateTo;
      private final Boolean isIsolated;
      private final Boolean useTransitive;
      private final String repo;
      private final String id;

      public LibraryDef(String coordinates, String relocateFrom, String relocateTo, Boolean isIsolated, Boolean useTransitive, String repo, String id) {
         this.coordinates = coordinates;
         this.relocateFrom = relocateFrom;
         this.relocateTo = relocateTo;
         this.isIsolated = isIsolated;
         this.useTransitive = useTransitive;
         this.repo = repo;
         this.id = id;
      }

      public String coordinates() {
         return this.coordinates;
      }

      public String relocateFrom() {
         return this.relocateFrom;
      }

      public String relocateTo() {
         return this.relocateTo;
      }

      public Boolean isIsolated() {
         return this.isIsolated;
      }

      public Boolean useTransitive() {
         return this.useTransitive;
      }

      public String repo() {
         return this.repo;
      }

      public String id() {
         return this.id;
      }
   }

   public static final class PermissionDef {
      private final String node;
      private final String description;
      private final String defaultValue;

      public PermissionDef(String node, String description, String defaultValue) {
         this.node = node;
         this.description = description;
         this.defaultValue = defaultValue;
      }

      public String node() {
         return this.node;
      }

      public String description() {
         return this.description;
      }

      public String defaultValue() {
         return this.defaultValue;
      }
   }
}
