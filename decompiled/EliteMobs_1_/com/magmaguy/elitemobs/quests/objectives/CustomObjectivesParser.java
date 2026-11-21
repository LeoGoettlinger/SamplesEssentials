package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CustomObjectivesParser {
   public static List<Objective> processCustomObjectives(CustomQuest customQuest) {
      return processCustomObjectives(customQuest.getCustomQuestsConfigFields());
   }

   public static List<Objective> processCustomObjectives(CustomQuestsConfigFields fields) {
      List<Objective> objectives = new ArrayList<>();

      for (Map<String, Object> maps : fields.getCustomObjectives().values()) {
         Objective objective = processObjectiveType(maps, fields);
         if (objective == null) {
            throw new IllegalArgumentException("Invalid objective in " + fields.getFilename());
         }

         objectives.add(objective);
      }

      return objectives;
   }

   private static Objective processObjectiveType(Map<String, Object> rawMap, CustomQuestsConfigFields fields) {
      CustomObjectivesParser.ObjectiveType objectiveType = null;
      String filename = null;
      String location = null;
      List<String> dialog = null;
      String name = null;
      String classId = null;
      Integer amount = 1;

      for (Entry<String, Object> entry : rawMap.entrySet()) {
         String var11 = entry.getKey();
         switch (var11) {
            case "class":
               classId = MapListInterpreter.parseString(entry.getKey(), entry.getValue(), fields.getFilename());
               break;
            case "objectiveType":
               objectiveType = MapListInterpreter.parseEnum(entry.getKey(), entry.getValue(), CustomObjectivesParser.ObjectiveType.class, fields.getFilename());
               break;
            case "filename":
               filename = MapListInterpreter.parseString(entry.getKey(), entry.getValue(), fields.getFilename());
               break;
            case "amount":
               amount = MapListInterpreter.parseInteger(entry.getKey(), entry.getValue(), fields.getFilename());
               if (amount != null && amount >= 1) {
                  break;
               }

               throw new IllegalArgumentException("Objective amount must be positive in " + fields.getFilename());
            case "location":
               location = MapListInterpreter.parseString(entry.getKey(), entry.getValue(), fields.getFilename());
               break;
            case "dialog":
               dialog = ChatColorConverter.convert(MapListInterpreter.parseStringList(entry.getKey(), entry.getValue(), fields.getFilename()));
               break;
            case "npcName":
            case "itemName":
            case "name":
               name = ChatColorConverter.convert(MapListInterpreter.parseString(entry.getKey(), entry.getValue(), fields.getFilename()));
         }
      }

      if (objectiveType == CustomObjectivesParser.ObjectiveType.FETCH_ITEM && name == null) {
         try {
            name = CustomItem.getCustomItem(filename).getCustomItemsConfigFields().getName();
         } catch (Exception var13) {
            Logger.warn(
               "Failed to get name for custom item "
                  + filename
                  + " in Custom Quest "
                  + fields.getFilename()
                  + " . This objective will not display the item name."
            );
         }
      }

      if (objectiveType == CustomObjectivesParser.ObjectiveType.CLASS_UNLOCK) {
         ClassFormDefinition form = classId != null && !classId.isBlank() ? BuiltInClassContent.catalog().find(classId).orElse(null) : null;
         if (form != null && filename != null && !filename.isBlank()) {
            return new ClassUnlockObjective(form.id(), name == null ? form.displayName() : name, filename);
         } else {
            Logger.warn(
               "Invalid CLASS_UNLOCK objective in "
                  + fields.getFilename()
                  + ": a valid class and trainer filename are required. The objective cannot be completed."
            );
            return new ClassUnlockObjective(null, name == null ? "Unknown class" : name, filename);
         }
      } else if (filename == null) {
         Logger.warn("Invalid filename for entry " + rawMap + " in Custom Quest " + fields.getFilename() + " . This objective will not be registered.");
         return null;
      } else {
         try {
            if (objectiveType.equals(CustomObjectivesParser.ObjectiveType.KILL_CUSTOM)) {
               return new CustomKillObjective(filename, amount, fields.getQuestLevel());
            }

            if (objectiveType.equals(CustomObjectivesParser.ObjectiveType.FETCH_ITEM)) {
               return new CustomFetchObjective(amount, name, filename);
            }

            if (objectiveType.equals(CustomObjectivesParser.ObjectiveType.DIALOG)) {
               if (name == null || name.isEmpty()) {
                  NPCsConfigFields npcConfig = NPCsConfig.getNpcEntities().get(filename);
                  if (npcConfig != null && npcConfig.getName() != null) {
                     name = npcConfig.getName();
                  }
               }

               return new DialogObjective(filename, name, location, dialog);
            }

            if (objectiveType.equals(CustomObjectivesParser.ObjectiveType.ARENA)) {
               return new ArenaObjective(name, filename);
            }
         } catch (Exception var14) {
            Logger.warn("Failed to register objective type for quest " + fields.getFilename() + " ! This quest will be skipped");
            Logger.warn("Invalid entry: " + rawMap);
            var14.printStackTrace();
         }

         return null;
      }
   }

   private static enum ObjectiveType {
      KILL_CUSTOM,
      FETCH_ITEM,
      DIALOG,
      ARENA,
      CLASS_UNLOCK;
   }
}
