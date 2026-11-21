package net.thenextlvl.protect.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.RegionizedArea;
import net.thenextlvl.protect.exception.UnsupportedRegionException;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaCreateCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("create")
            .requires(stack -> stack.getSender() instanceof Player player && player.hasPermission("protect.command.area.create")))
         .then(
            ((RequiredArgumentBuilder)Commands.argument("name", StringArgumentType.string())
                  .then(Commands.argument("priority", IntegerArgumentType.integer()).executes(context -> {
                     Integer priority = (Integer)context.getArgument("priority", int.class);
                     return execute(context, priority, plugin);
                  })))
               .executes(context -> execute(context, 0, plugin))
         );
   }

   private static int execute(CommandContext<CommandSourceStack> context, int priority, ProtectPlugin plugin) {
      Player player = (Player)((CommandSourceStack)context.getSource()).getSender();
      String name = (String)context.getArgument("name", String.class);
      if (plugin.areaProvider().getArea(name).isPresent()) {
         plugin.bundle().sendMessage(player, "area.exists", Placeholder.parsed("area", name));
         return 0;
      } else {
         try {
            LocalSession session = WorldEditPlugin.getInstance().getSession(player);
            RegionizedArea<Region> creator = plugin.areaService().creator(name, player.getWorld(), session.getSelection()).priority(priority).create();
            plugin.bundle().sendMessage(player, "area.created", Placeholder.parsed("area", creator.getName()));
            return 1;
         } catch (UnsupportedRegionException var7) {
            plugin.bundle().sendMessage(player, "region.unsupported", Placeholder.parsed("type", var7.getType().getSimpleName()));
            return 0;
         } catch (IncompleteRegionException var8) {
            plugin.bundle().sendMessage(player, "region.select");
            return 0;
         } catch (Exception var9) {
            plugin.bundle().sendMessage(player, "command.error");
            plugin.getComponentLogger().error("Failed to create area {}", name, var9);
            ProtectPlugin.ERROR_TRACKER.trackError(var9);
            return 0;
         }
      }
   }
}
