package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class RelativeCoordinatesCommand {
   public static void get(Player player, String minidungeonString) {
      EMPackage emPackage = EMPackage.getEmPackages().get(minidungeonString);
      if (emPackage == null) {
         player.sendMessage(CommandMessagesConfig.getInvalidMinidungeonMessage().replace("$name", minidungeonString));
      } else if (!emPackage.isInstalled()) {
         player.sendMessage(CommandMessagesConfig.getMinidungeonNotInstalledMessage());
      } else {
         Location anchorpoint = emPackage.getContentPackagesConfigFields().getAnchorPoint();
         if (anchorpoint == null) {
            player.sendMessage(CommandMessagesConfig.getInvalidAnchorPointMessage());
         } else {
            Vector relativeVector = player.getLocation().clone().subtract(anchorpoint).toVector();
            if (emPackage.getContentPackagesConfigFields().getCalculatedRotation() != 0) {
               GenericRotationMatrixMath.rotateVectorYAxis(
                  emPackage.getContentPackagesConfigFields().getCalculatedRotation().intValue(), anchorpoint, relativeVector
               );
            }

            String relativePosition = relativeVector.getBlockX() + ", " + relativeVector.getBlockY() + ", " + relativeVector.getBlockZ();
            player.sendMessage(
               ChatColorConverter.convert(
                  CommandMessagesConfig.getRelativePositionMessage()
                     .replace("$filename", emPackage.getContentPackagesConfigFields().getName())
                     .replace("$coordinates", relativePosition)
               )
            );
         }
      }
   }
}
