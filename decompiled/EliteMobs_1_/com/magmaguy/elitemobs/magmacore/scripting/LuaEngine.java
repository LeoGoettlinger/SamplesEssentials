package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.magmacore.config.ContentFileSelector;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class LuaEngine {
   private static final Map<String, ScriptProvider> providers = new ConcurrentHashMap<>();
   private static final Map<String, ScriptDefinition> definitions = new ConcurrentHashMap<>();

   private LuaEngine() {
   }

   public static synchronized void registerScriptProvider(ScriptProvider provider) {
      Objects.requireNonNull(provider, "provider");
      Map<String, ScriptDefinition> discovered = discoverScripts(provider);
      String prefix = provider.getNamespace() + ":";
      providers.put(provider.getNamespace(), provider);
      definitions.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix));
      definitions.putAll(discovered);
   }

   public static synchronized void unregisterScriptProvider(String namespace) {
      providers.remove(namespace);
      definitions.entrySet().removeIf(e -> e.getKey().startsWith(namespace + ":"));
   }

   public static synchronized ScriptDefinition getDefinition(String namespace, String fileName) {
      return definitions.get(namespace + ":" + fileName);
   }

   public static synchronized Collection<ScriptDefinition> getDefinitions(String namespace) {
      List<ScriptDefinition> result = new ArrayList<>();
      String prefix = namespace + ":";

      for (Entry<String, ScriptDefinition> entry : definitions.entrySet()) {
         if (entry.getKey().startsWith(prefix)) {
            result.add(entry.getValue());
         }
      }

      return result;
   }

   public static synchronized ScriptDefinition loadScript(String namespace, File file) throws IOException {
      ScriptProvider provider = providers.get(namespace);
      if (provider == null) {
         throw new IllegalStateException("No script provider registered for namespace: " + namespace);
      } else {
         ScriptDefinition definition = validateScript(file, provider);
         definitions.put(namespace + ":" + file.getName(), definition);
         return definition;
      }
   }

   public static synchronized void shutdown() {
      definitions.clear();
      providers.clear();
   }

   private static Map<String, ScriptDefinition> discoverScripts(ScriptProvider provider) {
      Map<String, ScriptDefinition> discovered = new LinkedHashMap<>();
      File dir = provider.getScriptDirectory().toFile();
      if (dir.exists() && dir.isDirectory()) {
         discoverDirectory(dir, provider, discovered);
         return discovered;
      } else {
         return discovered;
      }
   }

   private static void discoverDirectory(File directory, ScriptProvider provider, Map<String, ScriptDefinition> discovered) {
      List<File> files;
      try (Stream<Path> paths = Files.walk(directory.toPath())) {
         files = paths.filter(x$0 -> Files.isRegularFile(x$0))
            .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".lua"))
            .map(Path::toFile)
            .toList();
      } catch (IOException var12) {
         Logger.warn("Cannot scan script directory " + directory + ": " + var12.getMessage());
         return;
      }

      for (File file : ContentFileSelector.select(files, name -> name.toLowerCase(Locale.ROOT))) {
         try {
            ScriptDefinition definition = validateScript(file, provider);
            discovered.put(provider.getNamespace() + ":" + file.getName(), definition);
         } catch (IOException var8) {
            Logger.warn("Skipping unreadable script " + file + ": " + var8.getMessage());
         } catch (IllegalArgumentException var9) {
            Logger.warn("Skipping invalid or retired script " + file + ": " + var9.getMessage());
         } catch (Exception var10) {
            Logger.warn("Failed to load script: " + file.getName());
            var10.printStackTrace();
         }
      }
   }

   private static ScriptDefinition validateScript(File file, ScriptProvider provider) throws IOException {
      String source = Files.readString(file.toPath(), StandardCharsets.UTF_8).replace("\r", "");
      return ScriptDefinition.validate(file.getName(), file, source, provider);
   }
}
