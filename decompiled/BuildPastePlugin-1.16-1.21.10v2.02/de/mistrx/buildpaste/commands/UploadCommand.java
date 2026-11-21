package de.mistrx.buildpaste.commands;

import de.mistrx.buildpaste.firebase.Firebase;
import de.mistrx.buildpaste.util.Debug;
import de.mistrx.buildpaste.util.Functions;
import de.mistrx.buildpaste.util.Variables;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class UploadCommand implements CommandExecutor {
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (sender instanceof Player) {
         String buildname = "";
         if (args.length >= 1) {
            buildname = String.join("%20", args);
         }

         Player player = (Player)sender;
         if (!Functions.checkPermissions(player, "upload")) {
            return false;
         }

         String uuid = player.getUniqueId().toString();
         World world = player.getWorld();
         Location pos1Location = Variables.GetPos1(uuid);
         Location pos2Location = Variables.GetPos2(uuid);
         if (pos1Location == null && pos2Location == null) {
            player.sendMessage(
               "Select two positions before uploading. You can do this by either typing /pos1 or /pos2 or by using the position selector item which you can obtain by typing /selector"
            );
            return false;
         }

         if (pos1Location == null) {
            player.sendMessage("Pos1 isn't selected");
            return false;
         }

         if (pos2Location == null) {
            player.sendMessage("Pos2 isn't selected");
            return false;
         }

         if (pos1Location.equals(pos2Location)) {
            player.sendMessage("Both positions are on the same spot. That's illegal!");
            return false;
         }

         Vector pos1 = new Vector(pos1Location.getBlockX(), pos1Location.getBlockY(), pos1Location.getBlockZ());
         Vector pos2 = new Vector(pos2Location.getBlockX(), pos2Location.getBlockY(), pos2Location.getBlockZ());
         List<Integer> size = new ArrayList<>();
         double xSmall;
         double xLarge;
         if (pos1.getBlockX() >= pos2.getBlockX()) {
            xLarge = pos1.getBlockX();
            xSmall = pos2.getBlockX();
         } else {
            xLarge = pos2.getBlockX();
            xSmall = pos1.getBlockX();
         }

         double ySmall;
         double yLarge;
         if (pos1.getBlockY() >= pos2.getBlockY()) {
            yLarge = pos1.getBlockY();
            ySmall = pos2.getBlockY();
         } else {
            yLarge = pos2.getBlockY();
            ySmall = pos1.getBlockY();
         }

         double zSmall;
         double zLarge;
         if (pos1.getBlockZ() >= pos2.getBlockZ()) {
            zLarge = pos1.getBlockZ();
            zSmall = pos2.getBlockZ();
         } else {
            zLarge = pos2.getBlockZ();
            zSmall = pos1.getBlockZ();
         }

         size.add((int)Math.round(xLarge + 1.0 - xSmall));
         size.add((int)Math.round(yLarge + 1.0 - ySmall));
         size.add((int)Math.round(zLarge + 1.0 - zSmall));
         List<Object> blocks = new ArrayList<>();
         List<String> blockData = new ArrayList<>();
         String nbtData = "{";
         String lookDirection = "south";
         lookDirection = Functions.getLookDirection(player.getLocation().getYaw());
         Integer index = 0;
         if (lookDirection.equals("north")) {
            for (double x = xLarge; x > xSmall - 1.0; x--) {
               for (double y = ySmall; y < yLarge + 1.0; y++) {
                  for (double z = zLarge; z > zSmall - 1.0; z--) {
                     Vector blockPos = new Vector(x, y, z);
                     Block block = world.getBlockAt(blockPos.getBlockX(), blockPos.getBlockY(), blockPos.getBlockZ());
                     String blockname = block.getType().toString().toLowerCase();
                     Debug.Log(blockname);
                     Integer blockID = Functions.getBlockIdByName(blockname.replace("minecraft:", ""));
                     if (!blockID.equals(null)) {
                        blocks.add(blockID);
                     } else {
                        blocks.add("\"" + blockname + "\"");
                     }

                     String data = Functions.getData(world, new Vector(blockPos.getBlockX(), blockPos.getBlockY(), blockPos.getBlockZ()));
                     if (data != null) {
                        blockData.add("\"" + data + "\"");
                     } else {
                        blockData.add(null);
                     }

                     index = index + 1;
                  }
               }
            }
         } else if (lookDirection.equals("east")) {
            for (double x = xSmall; x < xLarge + 1.0; x++) {
               for (double y = ySmall; y < yLarge + 1.0; y++) {
                  for (double z = zLarge; z > zSmall - 1.0; z--) {
                     Vector blockPosx = new Vector(x, y, z);
                     Block blockx = world.getBlockAt(blockPosx.getBlockX(), blockPosx.getBlockY(), blockPosx.getBlockZ());
                     String blocknamex = blockx.getType().toString().toLowerCase();
                     Debug.Log(blocknamex);
                     Integer blockIDx = Functions.getBlockIdByName(blocknamex.replace("minecraft:", ""));
                     if (!blockIDx.equals(null)) {
                        blocks.add(blockIDx);
                     } else {
                        blocks.add("\"" + blocknamex + "\"");
                     }

                     String data = Functions.getData(world, new Vector(blockPosx.getBlockX(), blockPosx.getBlockY(), blockPosx.getBlockZ()));
                     if (data != null) {
                        blockData.add("\"" + data + "\"");
                     } else {
                        blockData.add(null);
                     }

                     index = index + 1;
                  }
               }
            }
         } else if (lookDirection.equals("south")) {
            for (double x = xSmall; x < xLarge + 1.0; x++) {
               for (double y = ySmall; y < yLarge + 1.0; y++) {
                  for (double z = zSmall; z < zLarge + 1.0; z++) {
                     Vector blockPosxx = new Vector(x, y, z);
                     Block blockxx = world.getBlockAt(blockPosxx.getBlockX(), blockPosxx.getBlockY(), blockPosxx.getBlockZ());
                     String blocknamexx = blockxx.getType().toString().toLowerCase();
                     Debug.Log(blocknamexx);
                     Integer blockIDxx = Functions.getBlockIdByName(blocknamexx.replace("minecraft:", ""));
                     if (!blockIDxx.equals(null)) {
                        blocks.add(blockIDxx);
                     } else {
                        blocks.add("\"" + blocknamexx + "\"");
                     }

                     String data = Functions.getData(world, new Vector(blockPosxx.getBlockX(), blockPosxx.getBlockY(), blockPosxx.getBlockZ()));
                     if (data != null) {
                        blockData.add("\"" + data + "\"");
                     } else {
                        blockData.add(null);
                     }

                     index = index + 1;
                  }
               }
            }
         } else {
            if (!lookDirection.equals("west")) {
               player.sendMessage("An error occurred");
               return false;
            }

            for (double x = xLarge; x > xSmall - 1.0; x--) {
               for (double y = ySmall; y < yLarge + 1.0; y++) {
                  for (double z = zSmall; z < zLarge + 1.0; z++) {
                     Vector blockPosxxx = new Vector(x, y, z);
                     Block blockxxx = world.getBlockAt(blockPosxxx.getBlockX(), blockPosxxx.getBlockY(), blockPosxxx.getBlockZ());
                     String blocknamexxx = blockxxx.getType().toString().toLowerCase();
                     Debug.Log(blocknamexxx);
                     Integer blockIDxxx = Functions.getBlockIdByName(blocknamexxx.replace("minecraft:", ""));
                     if (!blockIDxxx.equals(null)) {
                        blocks.add(blockIDxxx);
                     } else {
                        blocks.add("\"" + blocknamexxx + "\"");
                     }

                     String data = Functions.getData(world, new Vector(blockPosxxx.getBlockX(), blockPosxxx.getBlockY(), blockPosxxx.getBlockZ()));
                     if (data != null) {
                        blockData.add("\"" + data + "\"");
                     } else {
                        blockData.add(null);
                     }

                     index = index + 1;
                  }
               }
            }
         }

         nbtData = nbtData + "}";
         String buildname_ = buildname.replaceAll("/", " ");
         Debug.Log(buildname_.toString());
         String buildID = "-";

         try {
            buildID = Firebase.sendStructureData(uuid, blocks, blockData, nbtData, size, lookDirection, buildname_);
         } catch (IOException var42) {
            var42.printStackTrace();
         }

         if (buildID == "-") {
            player.sendMessage(ChatColor.RED + "You selected too many blocks");
         } else {
            Variables.SetUploadedBuildID(uuid, buildID);
            Variables.SetPos1(uuid, null);
            Variables.SetPos2(uuid, null);
            String url = Variables.url + "/upload" + "?build=" + buildID;
            TextComponent message = new TextComponent("Your upload was successful!\n");
            TextComponent finishUpload = new TextComponent("Publish");
            finishUpload.setColor(ChatColor.GREEN);
            finishUpload.setClickEvent(new ClickEvent(Action.OPEN_URL, url));
            finishUpload.setHoverEvent(
               new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("Make your build available for everyone!")})
            );
            TextComponent copyBuild = new TextComponent("Copy");
            copyBuild.setColor(ChatColor.AQUA);
            copyBuild.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/setbuild " + buildID));
            copyBuild.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("Copy your build")}));
            TextComponent shareBuild = new TextComponent("Share");
            shareBuild.setColor(ChatColor.LIGHT_PURPLE);
            shareBuild.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/sharebuild " + buildID));
            shareBuild.setHoverEvent(
               new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("Share your build with the server")})
            );
            message.addExtra("[");
            message.addExtra(finishUpload);
            message.addExtra("] [");
            message.addExtra(copyBuild);
            message.addExtra("] [");
            message.addExtra(shareBuild);
            message.addExtra("]");
            player.spigot().sendMessage(message);
         }
      }

      return false;
   }
}
