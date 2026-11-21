package com.viaversion.viabackwards.api.data;

import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TranslatableMappings {
   private static final Map<String, Map<String, String>> TRANSLATABLES = new HashMap<>();

   public static void loadTranslatables() {
      if (!TRANSLATABLES.isEmpty()) {
         throw new IllegalStateException("Translatables already loaded!");
      } else {
         fillTranslatables(BackwardsMappingDataLoader.INSTANCE.loadFromDataDir("translation-mappings.json"), TRANSLATABLES);
      }
   }

   public static void fillTranslatables(JsonObject jsonObject, Map<String, Map<String, String>> translatables) {
      for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
         Map<String, String> versionMappings = new HashMap<>();
         translatables.put(entry.getKey(), versionMappings);

         for (Entry<String, JsonElement> translationEntry : entry.getValue().getAsJsonObject().entrySet()) {
            versionMappings.put(translationEntry.getKey(), translationEntry.getValue().getAsString());
         }
      }
   }

   public static Map<String, String> translatablesFor(Protocol<?, ?, ?, ?> protocol) {
      String version = protocol.getClass().getSimpleName().replace("Protocol", "").split("To")[0].replace("_", ".");
      return translatablesFor(version);
   }

   public static Map<String, String> translatablesFor(String version) {
      Map<String, String> translatableMappings = getTranslatableMappings(version);
      if (translatableMappings == null) {
         ViaBackwards.getPlatform().getLogger().warning("Missing " + version + " translatables!");
         return new HashMap<>();
      } else {
         return translatableMappings;
      }
   }

   @Nullable
   public static Map<String, String> getTranslatableMappings(String sectionIdentifier) {
      return TRANSLATABLES.get(sectionIdentifier);
   }
}
