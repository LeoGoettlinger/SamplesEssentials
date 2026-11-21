package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;

public final class DifficultyResolver {
   private final String source;
   private final boolean easyMediumHard;
   private final Map<String, String> declaredIds = new HashMap<>();
   private final Set<String> warnings = new HashSet<>();

   public DifficultyResolver(String source, List<Map<String, Object>> definitions) {
      this.source = source;
      List<Map<String, Object>> modes = definitions == null ? List.of() : definitions;
      Set<String> ids = new HashSet<>();
      Set<String> names = new HashSet<>();

      for (Map<String, Object> mode : modes) {
         ids.add(normalize(mode.get("id")));
         names.add(normalize(mode.get("name")));
      }

      boolean namedStandardIds = ids.contains("normal") || ids.contains("mythic");
      this.easyMediumHard = !namedStandardIds
         && (
            ids.contains("easy")
               || ids.contains("medium")
               || !names.contains("normal") && !names.contains("mythic") && (names.contains("easy") || names.contains("medium"))
         );

      for (Map<String, Object> mode : modes) {
         String id = normalize(mode.get("id"));
         String resolved = this.knownId(id);
         if (resolved == null) {
            resolved = this.knownId(normalize(mode.get("name")));
         }

         if (resolved == null) {
            this.warn(
               "definition:" + id,
               "Unrecognized difficulty id '"
                  + mode.get("id")
                  + "', name '"
                  + mode.get("name")
                  + "' in "
                  + source
                  + ". Expected 0/1/2, Normal/Hard/Mythic or Easy/Medium/Hard. Custom filters retain exact-ID matching; automatic loot uses its configured difficultyIds mapping or defaultDifficulty."
            );
         }

         if (!id.isEmpty() && resolved != null) {
            this.declaredIds.put(id, resolved);
         }
      }
   }

   public String resolve(Object raw) {
      String id = normalize(raw);
      String declared = this.declaredIds.get(id);
      if (declared != null) {
         return declared;
      } else {
         String known = this.knownId(id);
         return known == null ? id : known;
      }
   }

   public String resolveSelected(Object raw) {
      String resolved = this.resolve(raw);
      if (!isCanonical(resolved)) {
         this.warn(
            "definition:" + normalize(raw),
            "Unrecognized or missing difficultyID '"
               + raw
               + "' in "
               + this.source
               + ". Automatic loot uses its configured difficultyIds mapping or defaultDifficulty."
         );
      }

      return resolved;
   }

   public boolean matches(List<String> filter, Object selectedId, String filterSource) {
      String selected = this.resolve(selectedId);
      boolean matches = false;

      for (String value : filter) {
         String resolved = this.resolve(value);
         if (!isCanonical(resolved)) {
            this.warn(
               filterSource + ":" + value,
               "Unrecognized difficultyID '"
                  + value
                  + "' in "
                  + filterSource
                  + " for "
                  + this.source
                  + ". Expected 0/1/2, Normal/Hard/Mythic or Easy/Medium/Hard; using exact-ID matching."
            );
         }

         if (!resolved.isEmpty() && resolved.equals(selected)) {
            matches = true;
         }
      }

      return matches;
   }

   public static List<String> parseFilter(Object raw, String source) {
      List<?> values = raw instanceof List<?> list ? list : (raw == null ? List.of() : List.of(raw));
      List<String> result = new ArrayList<>();

      for (Object value : values) {
         if (!(value instanceof String) && !(value instanceof Number)) {
            Logger.warn("Invalid difficultyID in " + source + ": expected an ID or list of IDs; ignoring " + value);
         } else {
            String id = normalize(value);
            if (!id.isEmpty()) {
               result.add(id);
            }
         }
      }

      if (result.isEmpty()) {
         Logger.warn("Empty or invalid difficultyID in " + source + "; this filter matches no difficulty.");
      }

      return List.copyOf(result);
   }

   public static boolean isCanonical(String id) {
      return "0".equals(id) || "1".equals(id) || "2".equals(id);
   }

   private String knownId(String value) {
      return switch (value) {
         case "0", "normal", "easy" -> "0";
         case "1", "medium" -> "1";
         case "hard" -> this.easyMediumHard ? "2" : "1";
         case "2", "mythic" -> "2";
         default -> null;
      };
   }

   private static String normalize(Object value) {
      return value == null ? "" : ChatColor.stripColor(ChatColorConverter.convert(value.toString())).strip().toLowerCase(Locale.ROOT);
   }

   private void warn(String key, String message) {
      if (this.warnings.add(key)) {
         Logger.warn(message);
      }
   }
}
