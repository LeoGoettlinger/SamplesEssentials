package net.thenextlvl.protect.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.nio.file.Files;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.RegionizedArea;
import net.thenextlvl.protect.commands.argument.RegionizedAreaArgumentType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaRedefineCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("redefine")
            .requires(stack -> stack.getSender() instanceof Player player && player.hasPermission("protect.command.area.redefine")))
         .then(Commands.argument("area", new RegionizedAreaArgumentType(plugin)).executes(context -> redefine(context, plugin)));
   }

   private static int redefine(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      Player player = (Player)((CommandSourceStack)context.getSource()).getSender();
      RegionizedArea area = (RegionizedArea)context.getArgument("area", RegionizedArea.class);
      if (Files.isRegularFile(area.getSchematicFile())) {
         plugin.bundle().sendMessage(player, "area.schematic.delete");
         return 0;
      } else {
         try {
            WorldEditPlugin worldEdit = (WorldEditPlugin)JavaPlugin.getPlugin(WorldEditPlugin.class);
            Region region = worldEdit.getSession(player).getSelection();
            Boolean redefine = region.getClass().isInstance(area.getRegion()) ? area.setRegion(region) : null;
            String message = redefine == null ? "area.redefine.unsupported" : (redefine ? "area.redefine.success" : "area.redefine.fail");
            plugin.bundle()
               .sendMessage(player, message, Placeholder.parsed("area", area.getName()), Placeholder.parsed("type", region.getClass().getSimpleName()));
            return Boolean.TRUE.equals(redefine) ? 1 : 0;
         } catch (IncompleteRegionException var8) {
            plugin.bundle().sendMessage(player, "region.select");
            return 0;
         }
      }
   }
}
