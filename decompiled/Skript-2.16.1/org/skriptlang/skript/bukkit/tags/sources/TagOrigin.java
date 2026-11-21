package org.skriptlang.skript.bukkit.tags.sources;

import java.util.Collection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.tags.TagModule;

public enum TagOrigin {
   BUKKIT,
   PAPER,
   SKRIPT,
   ANY;

   @Contract(
      pure = true
   )
   @NotNull
   public static String getFullPattern() {
      return TagModule.PAPER_TAGS_EXIST ? "[:minecraft|:datapack|:paper|custom:(custom|skript)]" : "[:minecraft|:datapack|custom:(custom|skript)]";
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   public static TagOrigin fromParseTags(@NotNull Collection<String> tags) {
      TagOrigin origin = ANY;
      if (tags.contains("minecraft") || tags.contains("datapack")) {
         origin = BUKKIT;
      } else if (tags.contains("paper")) {
         origin = PAPER;
      } else if (tags.contains("custom")) {
         origin = SKRIPT;
      }

      return origin;
   }

   public boolean matches(TagOrigin other) {
      return this == other || this == ANY || other == ANY;
   }

   @Contract(
      pure = true
   )
   @NotNull
   public String toString(boolean datapackOnly) {
      return switch (this) {
         case BUKKIT -> datapackOnly ? "datapack" : "minecraft";
         case PAPER -> "paper";
         case SKRIPT -> "custom";
         case ANY -> "";
      };
   }
}
