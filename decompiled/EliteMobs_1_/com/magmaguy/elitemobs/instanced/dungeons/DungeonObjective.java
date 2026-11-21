package com.magmaguy.elitemobs.instanced.dungeons;

import java.util.Locale;
import lombok.Generated;

public class DungeonObjective {
   protected boolean completed = false;
   protected DungeonInstance dungeonInstance;

   public DungeonObjective(DungeonInstance dungeonInstance, String objectiveString) {
      this.dungeonInstance = dungeonInstance;
   }

   public static DungeonObjective registerObjective(DungeonInstance dungeonInstance, String objectiveString) {
      return new DungeonKillTargetObjective(dungeonInstance, parse(objectiveString));
   }

   public static DungeonObjective.TargetDefinition parse(String value) {
      if (value != null && !value.isBlank()) {
         String input = value.trim();
         if (!input.contains("=") && input.toLowerCase(Locale.ROOT).endsWith(".yml")) {
            input = "filename=" + input;
         }

         String filename = null;
         Integer amount = null;

         for (String field : input.split(":", -1)) {
            String[] pair = field.split("=", -1);
            if (pair.length != 2) {
               throw new IllegalArgumentException("Invalid dungeon objective: " + value);
            }

            String key = pair[0].trim().toLowerCase(Locale.ROOT);
            String argument = pair[1].trim();
            switch (key) {
               case "filename":
                  if (filename != null || !argument.toLowerCase(Locale.ROOT).endsWith(".yml")) {
                     throw new IllegalArgumentException("Invalid or duplicate objective filename: " + value);
                  }

                  filename = argument;
                  break;
               case "amount":
                  if (amount != null) {
                     throw new IllegalArgumentException("Duplicate objective amount: " + value);
                  }

                  try {
                     amount = Integer.valueOf(argument);
                  } catch (NumberFormatException var14) {
                     throw new IllegalArgumentException("Invalid objective amount: " + value, var14);
                  }

                  if (amount < 1) {
                     throw new IllegalArgumentException("Objective amount must be positive: " + value);
                  }
                  break;
               case "clearpercentage":
               case "clearpercent":
                  throw new IllegalArgumentException("Percentage dungeon objectives are unsupported; use filename=<boss.yml>:amount=<count>: " + value);
               default:
                  throw new IllegalArgumentException("Unknown dungeon objective field: " + value);
            }
         }

         if (filename == null) {
            throw new IllegalArgumentException("Dungeon objective requires a filename: " + value);
         } else {
            return new DungeonObjective.TargetDefinition(filename, amount == null ? 1 : amount);
         }
      } else {
         throw new IllegalArgumentException("Empty dungeon objective");
      }
   }

   protected void initializeObjective(DungeonInstance dungeonInstance) {
      this.dungeonInstance = dungeonInstance;
   }

   public void unregister() {
   }

   @Generated
   public boolean isCompleted() {
      return this.completed;
   }

   @Generated
   public DungeonInstance getDungeonInstance() {
      return this.dungeonInstance;
   }

   public record TargetDefinition(String filename, int amount) {
   }
}
