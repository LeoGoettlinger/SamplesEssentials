package de.mistrx.buildpaste.commands;

import de.mistrx.buildpaste.util.Debug;
import de.mistrx.buildpaste.util.Functions;
import de.mistrx.buildpaste.util.Variables;
import java.util.HashMap;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ConstructCommand implements CommandExecutor {
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args_) {
      Player player = (Player)sender;
      String uuid = player.getUniqueId().toString();
      if (!Functions.checkPermissions(player, "construct")) {
         return false;
      } else {
         String firstarg = null;
         if (args_.length >= 1) {
            firstarg = args_[0];
         }

         String[] args = new String[]{firstarg, null};
         if (firstarg != null && firstarg.equals("constructbuild")) {
            String direction = Functions.getLookDirection(player.getLocation().getYaw());
            Vector pos = new Vector(Math.floor(player.getLocation().getX()), Math.floor(player.getLocation().getY()), Math.floor(player.getLocation().getZ()));
            Functions.pasteCurrentBuilding(
               uuid,
               pos,
               Variables.GetBuildSize(uuid),
               Variables.GetBuildDirection(uuid),
               Variables.GetBuildIDs(uuid),
               Variables.GetBuildData(uuid),
               Variables.GetBuildNBT(uuid),
               direction,
               "dontplaceair",
               false,
               true
            );
            player.sendMessage("Build constructed successfully");
            return true;
         } else {
            Variables.SetPlayerItems(uuid, new HashMap<>());
            Variables.SetBuildBlocks(uuid, new HashMap<>());
            ItemStack[] inventory = player.getInventory().getContents();
            Integer inventorySize = inventory.length;

            for (int i = 0; i < inventorySize; i++) {
               if (inventory[i] != null) {
                  String itemName = "minecraft:" + inventory[i].getType().name().toLowerCase();
                  Integer itemAmount = inventory[i].getAmount();
                  Variables.MergePlayerItem(uuid, itemName, itemAmount);
               }
            }

            String response = Functions.setBuilding(uuid, null, "south", args, false);
            if (response.equals("error")) {
               Functions.sendNotConnectedMessage(player);
               return false;
            } else if (response.equals("random-error")) {
               player.sendMessage("A random error occurred. Maybe try again or post a comment");
               return false;
            } else if (response.equals("error-404")) {
               player.sendMessage("This Build doesn't exist (404)");
               return false;
            } else {
               List<Object> blockIDs = Variables.GetBuildIDs(uuid);

               for (int ix = 0; ix < blockIDs.size(); ix++) {
                  String block = Functions.getItemFromBlock(Functions.getBlockByIdOrName(blockIDs.get(ix)));
                  Variables.MergeBuildBlocks(uuid, block);
               }

               Variables.RemoveFromBuildBlocks(uuid, "minecraft:air");
               Variables.RemoveFromPlayerItems(uuid, "minecraft:air");
               Boolean hasAllRequiredMaterials = true;
               Boolean hasAnyRequiredMaterials = false;
               TextComponent requiredMaterials = new TextComponent("Needed Materials:\n");
               Object[] buildBlocksKeys = Variables.GetBuildBlocks(uuid).keySet().toArray();
               HashMap<String, Integer> playerItems = Variables.GetPlayerItems(uuid);

               for (int ix = 0; ix < buildBlocksKeys.length; ix++) {
                  String key = buildBlocksKeys[ix].toString();
                  Integer amount = Variables.GetBuildBlocks(uuid).get(key);
                  Debug.Log(key + ": " + amount.toString());
                  String endText = "";
                  ChatColor color = ChatColor.WHITE;
                  if (playerItems.containsKey(key)) {
                     hasAnyRequiredMaterials = true;
                     if (playerItems.get(key) >= amount) {
                        color = ChatColor.GREEN;
                     } else {
                        endText = " (" + playerItems.get(key) + "/" + amount.toString() + ")";
                        hasAllRequiredMaterials = false;
                     }
                  } else {
                     hasAllRequiredMaterials = false;
                  }

                  requiredMaterials.addExtra(color + amount.toString() + " x " + key.replace("minecraft:", "") + endText + "\n");
               }

               String constructBuildCommand = "/construct constructbuild";
               TextComponent constructBuildComponent = new TextComponent("Construct");
               constructBuildComponent.setColor(ChatColor.LIGHT_PURPLE);
               constructBuildComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, constructBuildCommand));
               constructBuildComponent.setHoverEvent(
                  new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("Construct the build")})
               );
               requiredMaterials.addExtra("\n");
               if (hasAllRequiredMaterials) {
                  requiredMaterials.addExtra("You have all required materials!");
               } else if (hasAnyRequiredMaterials) {
                  requiredMaterials.addExtra("You don't have all required materials");
                  constructBuildComponent.setText("Construct Anyways");
               }

               requiredMaterials.addExtra("\n");
               requiredMaterials.addExtra("[");
               requiredMaterials.addExtra(constructBuildComponent);
               requiredMaterials.addExtra("]");
               player.spigot().sendMessage(requiredMaterials);
               return true;
            }
         }
      }
   }
}
