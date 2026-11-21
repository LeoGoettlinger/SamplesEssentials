package com.magmaguy.elitemobs.config.npcs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import org.bukkit.entity.Villager.Profession;

public abstract class ClassTrainerConfig extends NPCsConfigFields {
   private static final JsonObject SKINS = readSkins();

   protected ClassTrainerConfig(String root, String name, String className, String location, String greeting, String farewell) {
      this(root, name, className, location, greeting, farewell, root);
   }

   protected ClassTrainerConfig(String root, String name, String className, String location, String greeting, String farewell, String skin) {
      super(
         "class_trainer_" + root,
         true,
         "&6" + name,
         "&e<" + className + " Instructor>",
         Profession.NITWIT,
         "em_adventurers_guild," + location,
         List.of(greeting),
         List.of("Inspect your training\\nand specializations."),
         List.of(farewell),
         true,
         3.0,
         NPCInteractions.NPCInteractionType.CLASS_TRAINER
      );
      this.setClassRoot(root);
      this.setDisguise("custom:ag_class_" + root);
      this.setCustomDisguiseData(disguise(skin));
   }

   public static String disguise(String root) {
      return "player " + root + " setskin " + SKINS.getAsJsonObject(root).getAsJsonObject("profile");
   }

   private static JsonObject readSkins() {
      try {
         JsonObject var2;
         try (
            InputStream stream = Objects.requireNonNull(ClassTrainerConfig.class.getResourceAsStream("/class_trainers/skins.json"), "Missing trainer skins");
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
         ) {
            var2 = JsonParser.parseReader(reader).getAsJsonObject();
         }

         return var2;
      } catch (Exception var8) {
         throw new IllegalStateException("Cannot load bundled class trainer skins", var8);
      }
   }
}
