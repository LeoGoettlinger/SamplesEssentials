package net.coreprotect.command;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.thread.Scheduler;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatMessage;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.Teleport;
import net.coreprotect.utility.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand {
   protected static void runCommand(CommandSender player, boolean permission, String[] args) {
      int resultc = args.length;
      if (!permission) {
         Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
      } else if (!(player instanceof Player)) {
         Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.TELEPORT_PLAYERS));
      } else {
         if (ConfigHandler.teleportThrottle.get(player.getName()) != null) {
            Object[] lookupThrottle = ConfigHandler.teleportThrottle.get(player.getName());
            if ((Boolean)lookupThrottle[0] || System.currentTimeMillis() - (Long)lookupThrottle[1] < 500L) {
               Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.COMMAND_THROTTLED));
               return;
            }
         }

         if (resultc < 3) {
            Chat.sendMessage(
               player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, "/co teleport <world> <x> <y> <z>")
            );
         } else {
            String worldName = args[1];
            int wid = WorldUtils.matchWorld(worldName);
            if (wid == -1 && resultc >= 5) {
               Chat.sendMessage(player, new ChatMessage(Phrase.build(Phrase.WORLD_NOT_FOUND, worldName)).build());
            } else {
               Location location = ((Player)player).getLocation().clone();
               World world = location.getWorld();
               if (wid > -1) {
                  world = Bukkit.getServer().getWorld(WorldUtils.getWorldName(wid));
               }

               String x = null;
               String y = null;
               String z = null;

               for (int i = 1; i < args.length; i++) {
                  if (i != 1 || wid <= -1) {
                     if (x == null) {
                        x = args[i].replaceAll("[^0-9.\\-]", "");
                     } else if (z == null) {
                        z = args[i].replaceAll("[^0-9.\\-]", "");
                     } else if (y == null) {
                        y = z;
                        z = args[i].replaceAll("[^0-9.\\-]", "");
                     }
                  }
               }

               if (y == null) {
                  if (location.getBlockY() > 63) {
                     location.setY(63.0);
                  }

                  y = Double.toString(location.getY());
               }

               String xValidate = x.replaceAll("[^.\\-]", "");
               String yValidate = y.replaceAll("[^.\\-]", "");
               String zValidate = z.replaceAll("[^.\\-]", "");
               if (x.length() != 0
                  && x.length() < 12
                  && !x.equals(xValidate)
                  && y.length() != 0
                  && y.length() < 12
                  && !y.equals(yValidate)
                  && z.length() != 0
                  && z.length() < 12
                  && !z.equals(zValidate)) {
                  location.setWorld(world);
                  location.setX(Double.parseDouble(x));
                  location.setY(Double.parseDouble(y));
                  location.setZ(Double.parseDouble(z));
                  int chunkX = location.getBlockX() >> 4;
                  int chunkZ = location.getBlockZ() >> 4;
                  Scheduler.runTask(CoreProtect.getInstance(), () -> {
                     if (!location.getWorld().isChunkLoaded(chunkX, chunkZ)) {
                        location.getWorld().getChunkAt(location);
                     }

                     Teleport.performSafeTeleport((Player)player, location, true);
                  }, location);
                  ConfigHandler.teleportThrottle.put(player.getName(), new Object[]{false, System.currentTimeMillis()});
               } else {
                  Chat.sendMessage(
                     player,
                     Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, "/co teleport <world> <x> <y> <z>")
                  );
               }
            }
         }
      }
   }
}
