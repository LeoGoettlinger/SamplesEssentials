package net.coreprotect.command.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.coreprotect.language.Phrase;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

public class MaterialParser {
   public static List<Object> parseRestricted(CommandSender player, String[] inputArguments, List<Integer> argAction) {
      String[] argumentArray = (String[])inputArguments.clone();
      List<Object> restricted = new ArrayList<>();
      int count = 0;
      int next = 0;

      for (String argument : argumentArray) {
         if (count > 0) {
            argument = argument.trim().toLowerCase(Locale.ROOT);
            argument = argument.replaceAll("\\\\", "");
            argument = argument.replaceAll("'", "");
            if (argument.equals("i:")
               || argument.equals("include:")
               || argument.equals("item:")
               || argument.equals("items:")
               || argument.equals("b:")
               || argument.equals("block:")
               || argument.equals("blocks:")) {
               next = 4;
            } else if (next != 4
               && !argument.startsWith("i:")
               && !argument.startsWith("include:")
               && !argument.startsWith("item:")
               && !argument.startsWith("items:")
               && !argument.startsWith("b:")
               && !argument.startsWith("block:")
               && !argument.startsWith("blocks:")) {
               next = 0;
            } else {
               argument = argument.replaceAll("include:", "");
               argument = argument.replaceAll("i:", "");
               argument = argument.replaceAll("items:", "");
               argument = argument.replaceAll("item:", "");
               argument = argument.replaceAll("blocks:", "");
               argument = argument.replaceAll("block:", "");
               argument = argument.replaceAll("b:", "");
               if (argument.contains(",")) {
                  String[] i2 = argument.split(",");

                  for (String i3 : i2) {
                     if (!checkTags(i3, restricted)) {
                        Material i3_material = MaterialUtils.getType(i3);
                        if (i3_material == null || !i3_material.isBlock() && !argAction.contains(4)) {
                           EntityType i3_entity = EntityUtils.getEntityType(i3);
                           if (i3_entity != null) {
                              restricted.add(i3_entity);
                           } else {
                              if (i3_material == null) {
                                 Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_INCLUDE, i3));
                                 return null;
                              }

                              restricted.add(i3_material);
                           }
                        } else {
                           restricted.add(i3_material);
                        }
                     }
                  }

                  if (argument.endsWith(",")) {
                     next = 4;
                  } else {
                     next = 0;
                  }
               } else {
                  if (!checkTags(argument, restricted)) {
                     Material material = MaterialUtils.getType(argument);
                     if (material == null || !material.isBlock() && !argAction.contains(4)) {
                        EntityType entityType = EntityUtils.getEntityType(argument);
                        if (entityType != null) {
                           restricted.add(entityType);
                        } else {
                           if (material == null) {
                              Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_INCLUDE, argument));
                              return null;
                           }

                           restricted.add(material);
                        }
                     } else {
                        restricted.add(material);
                     }
                  }

                  next = 0;
               }
            }
         }

         count++;
      }

      return restricted;
   }

   public static Map<Object, Boolean> parseExcluded(CommandSender player, String[] inputArguments, List<Integer> argAction) {
      String[] argumentArray = (String[])inputArguments.clone();
      Map<Object, Boolean> excluded = new HashMap<>();
      int count = 0;
      int next = 0;

      for (String argument : argumentArray) {
         if (count > 0) {
            argument = argument.trim().toLowerCase(Locale.ROOT);
            argument = argument.replaceAll("\\\\", "");
            argument = argument.replaceAll("'", "");
            if (argument.equals("e:") || argument.equals("exclude:")) {
               next = 5;
            } else if (next != 5 && !argument.startsWith("e:") && !argument.startsWith("exclude:")) {
               next = 0;
            } else {
               argument = argument.replaceAll("exclude:", "");
               argument = argument.replaceAll("e:", "");
               if (argument.contains(",")) {
                  String[] i2 = argument.split(",");

                  for (String i3 : i2) {
                     if (!checkTags(i3, excluded)) {
                        Material i3_material = MaterialUtils.getType(i3);
                        if (i3_material == null || !i3_material.isBlock() && !argAction.contains(4)) {
                           EntityType i3_entity = EntityUtils.getEntityType(i3);
                           if (i3_entity != null) {
                              excluded.put(i3_entity, false);
                           } else if (i3_material != null) {
                              excluded.put(i3_material, false);
                           }
                        } else {
                           excluded.put(i3_material, false);
                        }
                     }
                  }

                  if (argument.endsWith(",")) {
                     next = 5;
                  } else {
                     next = 0;
                  }
               } else {
                  if (!checkTags(argument, excluded)) {
                     Material iMaterial = MaterialUtils.getType(argument);
                     if (iMaterial == null || !iMaterial.isBlock() && !argAction.contains(4)) {
                        EntityType iEntity = EntityUtils.getEntityType(argument);
                        if (iEntity != null) {
                           excluded.put(iEntity, false);
                        } else if (iMaterial != null) {
                           excluded.put(iMaterial, false);
                        }
                     } else {
                        excluded.put(iMaterial, false);
                     }
                  }

                  next = 0;
               }
            }
         }

         count++;
      }

      return excluded;
   }

   public static Map<String, Set<Material>> getTags() {
      Map<String, Set<Material>> tagMap = new HashMap<>();
      tagMap.put("#button", BlockGroup.BUTTONS);
      tagMap.put("#container", BlockGroup.CONTAINERS);
      tagMap.put("#door", BlockGroup.DOORS);
      tagMap.put("#natural", BlockGroup.NATURAL_BLOCKS);
      tagMap.put("#pressure_plate", BlockGroup.PRESSURE_PLATES);
      tagMap.put("#shulker_box", BlockGroup.SHULKER_BOXES);
      return tagMap;
   }

   public static boolean checkTags(String argument) {
      return getTags().containsKey(argument);
   }

   public static boolean checkTags(String argument, Map<Object, Boolean> list) {
      for (Entry<String, Set<Material>> entry : getTags().entrySet()) {
         String tag = entry.getKey();
         Set<Material> materials = entry.getValue();
         if (argument.equals(tag)) {
            for (Material block : materials) {
               list.put(block, false);
            }

            return true;
         }
      }

      return false;
   }

   public static boolean checkTags(String argument, List<Object> list) {
      for (Entry<String, Set<Material>> entry : getTags().entrySet()) {
         String tag = entry.getKey();
         Set<Material> materials = entry.getValue();
         if (argument.equals(tag)) {
            list.addAll(materials);
            return true;
         }
      }

      return false;
   }

   public static boolean isBlockOrEntity(String argument) {
      boolean isBlock = false;
      if (checkTags(argument)) {
         isBlock = true;
      } else {
         Material material = MaterialUtils.getType(argument);
         if (material != null) {
            isBlock = true;
         } else {
            EntityType entityType = EntityUtils.getEntityType(argument);
            if (entityType != null) {
               isBlock = true;
            }
         }
      }

      return isBlock;
   }
}
