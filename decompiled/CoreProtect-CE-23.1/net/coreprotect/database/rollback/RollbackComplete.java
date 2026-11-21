package net.coreprotect.database.rollback;

import java.lang.invoke.StringConcatFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

public class RollbackComplete {
   public static void output(
      CommandSender user,
      Location location,
      List<String> checkUsers,
      List<Object> restrictList,
      Map<Object, Boolean> excludeList,
      List<String> excludeUserList,
      List<Integer> actionList,
      String timeString,
      Integer chunkCount,
      Double seconds,
      Integer itemCount,
      Integer blockCount,
      Integer entityCount,
      int rollbackType,
      Integer[] radius,
      boolean verbose,
      boolean restrictWorld,
      int preview
   ) {
      try {
         if (preview == 2) {
            Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PREVIEW_CANCELLED));
            return;
         }

         Chat.sendMessage(user, "-----");
         StringBuilder usersBuilder = new StringBuilder();

         for (String value : checkUsers) {
            if (usersBuilder.length() == 0) {
               usersBuilder = usersBuilder.append(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(value));
            } else {
               usersBuilder.append(", ").append(value);
            }
         }

         String users = usersBuilder.toString();
         if (users.equals("#global") && restrictWorld) {
            users = "#" + location.getWorld().getName();
         }

         if (preview > 0) {
            Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_COMPLETED, users, "{3}"));
         } else if (rollbackType == 0) {
            Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_COMPLETED, users, "{1}"));
         } else if (rollbackType == 1) {
            Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_COMPLETED, users, "{2}"));
         }

         if (preview == 1 || rollbackType == 0 || rollbackType == 1) {
            Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_TIME, timeString));
         }

         if (radius != null) {
            int worldedit = radius[7];
            if (worldedit == 0) {
               Integer rollbackRadius = radius[0];
               Chat.sendMessage(
                  user,
                  Color.DARK_AQUA
                     + "CoreProtect "
                     + Color.WHITE
                     + "- "
                     + Phrase.build(Phrase.ROLLBACK_RADIUS, rollbackRadius.toString(), rollbackRadius == 1 ? "{1}" : "{2}")
               );
            } else {
               Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_SELECTION, "#worldedit"));
            }
         }

         if (restrictWorld && radius == null && location != null) {
            Chat.sendMessage(
               user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_WORLD_ACTION, location.getWorld().getName(), "{1}")
            );
         }

         if (actionList.contains(4) && actionList.contains(11)) {
            if (actionList.contains(0)) {
               Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_WORLD_ACTION, "+inventory", "{2}"));
            } else if (actionList.contains(1)) {
               Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_WORLD_ACTION, "-inventory", "{2}"));
            } else {
               Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_WORLD_ACTION, "inventory", "{2}"));
            }
         } else if (actionList.contains(4)) {
            if (actionList.contains(0)) {
               Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_WORLD_ACTION, "-container", "{2}"));
            } else if (actionList.contains(1)) {
               Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_WORLD_ACTION, "+container", "{2}"));
            } else {
               Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_WORLD_ACTION, "container", "{2}"));
            }
         } else if (actionList.contains(0) && actionList.contains(1)) {
            Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_WORLD_ACTION, "block", "{2}"));
         } else if (actionList.contains(0)) {
            Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_WORLD_ACTION, "-block", "{2}"));
         } else if (actionList.contains(1)) {
            Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_WORLD_ACTION, "+block", "{2}"));
         } else if (actionList.contains(3)) {
            Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.ROLLBACK_WORLD_ACTION, "kill", "{2}"));
         }

         if (restrictList.size() > 0) {
            StringBuilder restrictTargets = new StringBuilder();
            boolean material = false;
            boolean item = false;
            boolean entity = false;
            int targetCount = 0;

            for (Object restrictTarget : restrictList) {
               String targetName = "";
               if (restrictTarget instanceof Material) {
                  targetName = ((Material)restrictTarget).name().toLowerCase(Locale.ROOT);
                  item = !item ? !((Material)restrictTarget).isBlock() : item;
                  material = true;
               } else if (restrictTarget instanceof EntityType) {
                  targetName = ((EntityType)restrictTarget).name().toLowerCase(Locale.ROOT);
                  entity = true;
               }

               if (targetCount == 0) {
                  restrictTargets = restrictTargets.append(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(targetName));
               } else {
                  restrictTargets.append(", ").append(targetName);
               }

               targetCount++;
            }

            String targetType = "{3}";
            if (material && !item && !entity) {
               targetType = "{1}";
            } else if (material && item && !entity) {
               targetType = "{3}";
            } else if (entity && !material) {
               targetType = "{2}";
            }

            Chat.sendMessage(
               user,
               Color.DARK_AQUA
                  + "CoreProtect "
                  + Color.WHITE
                  + "- "
                  + Phrase.build(Phrase.ROLLBACK_INCLUDE, restrictTargets.toString(), "{1}", targetType, targetCount == 1 ? "{1}" : "{2}")
            );
         }

         if (excludeList.size() > 0) {
            StringBuilder excludeTargets = new StringBuilder();
            boolean material = false;
            boolean item = false;
            boolean entity = false;
            int excludeCount = 0;

            for (Entry<Object, Boolean> entry : excludeList.entrySet()) {
               Object excludeTarget = entry.getKey();
               Boolean excludeTargetInternal = entry.getValue();
               if (!Boolean.TRUE.equals(excludeTargetInternal)
                  && (
                     !actionList.contains(4)
                        || !actionList.contains(11)
                        || !excludeTarget.equals(Material.FIRE) && !excludeTarget.equals(Material.WATER) && !excludeTarget.equals(Material.FARMLAND)
                  )) {
                  String targetNamex = "";
                  if (excludeTarget instanceof Material) {
                     targetNamex = ((Material)excludeTarget).name().toLowerCase(Locale.ROOT);
                     item = !item ? !((Material)excludeTarget).isBlock() : item;
                     material = true;
                  } else if (excludeTarget instanceof EntityType) {
                     targetNamex = ((EntityType)excludeTarget).name().toLowerCase(Locale.ROOT);
                     entity = true;
                  }

                  if (excludeCount == 0) {
                     excludeTargets = excludeTargets.append(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(targetNamex));
                  } else {
                     excludeTargets.append(", ").append(targetNamex);
                  }

                  excludeCount++;
               }
            }

            String targetType = "{3}";
            if (material && !item && !entity) {
               targetType = "{1}";
            } else if (material && item && !entity) {
               targetType = "{3}";
            } else if (entity && !material) {
               targetType = "{2}";
            }

            if (excludeCount > 0) {
               Chat.sendMessage(
                  user,
                  Color.DARK_AQUA
                     + "CoreProtect "
                     + Color.WHITE
                     + "- "
                     + Phrase.build(Phrase.ROLLBACK_INCLUDE, excludeTargets.toString(), "{2}", targetType, excludeCount == 1 ? "{1}" : "{2}")
               );
            }
         }

         if (excludeUserList.size() > 0) {
            StringBuilder excludeUsers = new StringBuilder();
            int excludeCount = 0;

            for (String excludeUser : excludeUserList) {
               if (!actionList.contains(4) || !actionList.contains(11) || !excludeUser.equals("#hopper")) {
                  if (excludeCount == 0) {
                     excludeUsers = excludeUsers.append(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(excludeUser));
                  } else {
                     excludeUsers.append(", ").append(excludeUser);
                  }

                  excludeCount++;
               }
            }

            if (excludeCount > 0) {
               Chat.sendMessage(
                  user,
                  Color.DARK_AQUA
                     + "CoreProtect "
                     + Color.WHITE
                     + "- "
                     + Phrase.build(Phrase.ROLLBACK_EXCLUDED_USERS, excludeUsers.toString(), excludeCount == 1 ? "{1}" : "{2}")
               );
            }
         }

         StringBuilder modifiedData = new StringBuilder();
         Integer modifyCount = 0;
         if (actionList.contains(5)) {
            modifiedData = modifiedData.append(Phrase.build(Phrase.AMOUNT_ITEM, NumberFormat.getInstance().format(blockCount), blockCount == 1 ? "{1}" : "{2}"));
            modifyCount = modifyCount + 1;
         } else {
            if (itemCount > 0 || actionList.contains(4)) {
               modifiedData = modifiedData.append(
                  Phrase.build(Phrase.AMOUNT_ITEM, NumberFormat.getInstance().format(itemCount), itemCount == 1 ? "{1}" : "{2}")
               );
               modifyCount = modifyCount + 1;
            }

            if (entityCount > 0) {
               if (modifyCount > 0) {
                  modifiedData.append(", ");
               }

               modifiedData.append(Phrase.build(Phrase.AMOUNT_ENTITY, NumberFormat.getInstance().format(entityCount), entityCount == 1 ? "{1}" : "{2}"));
               modifyCount = modifyCount + 1;
            }

            if (blockCount > 0 || !actionList.contains(4) || preview > 0) {
               if (modifyCount > 0) {
                  modifiedData.append(", ");
               }

               modifiedData.append(Phrase.build(Phrase.AMOUNT_BLOCK, NumberFormat.getInstance().format(blockCount), blockCount == 1 ? "{1}" : "{2}"));
               modifyCount = modifyCount + 1;
            }
         }

         StringBuilder modifiedDataVerbose = new StringBuilder();
         if (verbose && preview == 0 && !actionList.contains(11)) {
            if (chunkCount > -1 && modifyCount < 3) {
               if (modifyCount > 0) {
                  modifiedData.append(", ");
               }

               modifiedData.append(Phrase.build(Phrase.AMOUNT_CHUNK, NumberFormat.getInstance().format(chunkCount), chunkCount == 1 ? "{1}" : "{2}"));
               modifyCount = modifyCount + 1;
            } else if (chunkCount > 1) {
               modifiedDataVerbose.append(Phrase.build(Phrase.AMOUNT_CHUNK, NumberFormat.getInstance().format(chunkCount), chunkCount == 1 ? "{1}" : "{2}"));
            }
         }

         Chat.sendMessage(
            user,
            Color.DARK_AQUA
               + "CoreProtect "
               + Color.WHITE
               + "- "
               + Phrase.build(Phrase.ROLLBACK_MODIFIED, modifiedData.toString(), preview == 0 ? "{1}" : "{2}")
         );
         if (modifiedDataVerbose.length() > 0) {
            Chat.sendMessage(
               user,
               Color.DARK_AQUA
                  + "CoreProtect "
                  + Color.WHITE
                  + "- "
                  + Phrase.build(Phrase.ROLLBACK_MODIFIED, modifiedDataVerbose.toString(), preview == 0 ? "{1}" : "{2}")
            );
         }

         if (preview == 0) {
            BigDecimal decimalSeconds = new BigDecimal(seconds).setScale(1, RoundingMode.HALF_EVEN);
            Chat.sendMessage(
               user,
               Color.DARK_AQUA
                  + "CoreProtect "
                  + Color.WHITE
                  + "- "
                  + Phrase.build(
                     Phrase.ROLLBACK_LENGTH, decimalSeconds.stripTrailingZeros().toPlainString(), decimalSeconds.doubleValue() == 1.0 ? "{1}" : "{2}"
                  )
            );
         }

         Chat.sendMessage(user, "-----");
         if (preview > 0) {
            Chat.sendMessage(user, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PLEASE_SELECT, "/co apply", "/co cancel"));
         }
      } catch (Exception var30) {
         var30.printStackTrace();
      }
   }
}
