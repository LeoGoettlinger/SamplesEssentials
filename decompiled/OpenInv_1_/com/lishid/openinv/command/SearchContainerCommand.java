package com.lishid.openinv.command;

import com.lishid.openinv.util.SearchHelper;
import com.lishid.openinv.util.TabCompleter;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.lang.Replacement;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SearchContainerCommand implements TabExecutor {
   @NotNull
   private final Plugin plugin;
   @NotNull
   private final LanguageManager lang;

   public SearchContainerCommand(@NotNull Plugin plugin, @NotNull LanguageManager lang) {
      this.plugin = plugin;
      this.lang = lang;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (!(sender instanceof Player senderPlayer)) {
         this.lang.sendMessage(sender, "messages.error.consoleUnsupported");
         return true;
      } else if (args.length < 1) {
         return false;
      } else {
         Material material = Material.matchMaterial(args[0]);
         if (material == null) {
            this.lang.sendMessage(sender, "messages.error.invalidMaterial", new Replacement("%target%", args[0]));
            return false;
         } else {
            int radius = 5;
            if (args.length > 1) {
               try {
                  radius = Integer.parseInt(args[1]);
               } catch (NumberFormatException var22) {
                  return false;
               }
            }

            int configMax = this.plugin.getConfig().getInt("settings.command.searchcontainer.max-radius", 10);
            radius = Math.max(0, Math.min(radius, configMax));
            World world = senderPlayer.getWorld();
            Chunk centerChunk = senderPlayer.getLocation().getChunk();
            StringBuilder locations = new StringBuilder();
            locations.append("\n");

            for (int dX = -radius; dX <= radius; dX++) {
               for (int dZ = -radius; dZ <= radius; dZ++) {
                  if (world.loadChunk(centerChunk.getX() + dX, centerChunk.getZ() + dZ, false)) {
                     Chunk chunk = world.getChunkAt(centerChunk.getX() + dX, centerChunk.getZ() + dZ);

                     for (BlockState tileEntity : chunk.getTileEntities()) {
                        if (tileEntity instanceof InventoryHolder holder
                           && SearchHelper.findMatch(holder.getInventory(), itemStack -> itemStack.getType() == material)) {
                           locations.append(holder.getInventory().getType().name().toLowerCase(Locale.ENGLISH)).append(": ");
                           if (holder instanceof Nameable named) {
                              String customName = named.getCustomName();
                              if (customName != null) {
                                 locations.append(customName.toLowerCase(Locale.ENGLISH).strip()).append(" - ");
                              }
                           }

                           locations.append(" (")
                              .append(tileEntity.getX())
                              .append(',')
                              .append(tileEntity.getY())
                              .append(',')
                              .append(tileEntity.getZ())
                              .append(")\n");
                        }
                     }
                  }
               }
            }

            if (locations.length() < 2) {
               this.lang.sendMessage(sender, "messages.info.container.noMatches", new Replacement("%target%", material.name()));
               return true;
            } else {
               this.lang
                  .sendMessage(
                     sender, "messages.info.container.matches", new Replacement("%target%", material.name()), new Replacement("%detail%", locations.toString())
                  );
               return true;
            }
         }
      }
   }

   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
      if (args.length >= 1 && args.length <= 2 && command.testPermissionSilent(sender)) {
         String argument = args[args.length - 1];
         return args.length == 1 ? TabCompleter.completeEnum(argument, Material.class) : TabCompleter.completeInteger(argument);
      } else {
         return Collections.emptyList();
      }
   }
}
