package net.thenextlvl.protect.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.RegionizedArea;
import net.thenextlvl.protect.commands.argument.AreaArgumentType;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaInfoCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("info")
               .requires(stack -> stack.getSender().hasPermission("protect.command.area.info")))
            .then(Commands.argument("area", new AreaArgumentType(plugin)).executes(context -> {
               Area area = (Area)context.getArgument("area", Area.class);
               return info(context, area, plugin);
            })))
         .executes(context -> {
            Location location = ((CommandSourceStack)context.getSource()).getLocation();
            Area area = plugin.areaProvider().getArea(location);
            return info(context, area, plugin);
         });
   }

   private static int info(CommandContext<CommandSourceStack> context, Area area, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      String type = plugin.areaService().getAdapter(area.getClass()).key().asString();
      List<Component> flags = area.getFlags()
         .entrySet()
         .stream()
         .map(
            entry -> plugin.bundle()
               .component(
                  "area.flag.format",
                  sender,
                  Placeholder.parsed("flag", entry.getKey().key().asString()),
                  Placeholder.unparsed("value", String.valueOf(entry.getValue()))
               )
         )
         .toList();
      TextComponent owner = area.getOwner()
         .map(plugin.getServer()::getOfflinePlayer)
         .map(OfflinePlayer::getName)
         .<TextComponent>map(Component::text)
         .orElseGet(Component::empty);
      long size = 0L;

      try {
         size = Files.size(area.getDataFile()) / 1024L;
      } catch (IOException var10) {
         plugin.getComponentLogger().warn("Failed to get file size of area {}", area.getName(), var10);
      }

      plugin.bundle()
         .sendMessage(
            sender,
            "area.info",
            Formatter.joining("flags", flags),
            Placeholder.parsed("area", area.getName()),
            Placeholder.parsed("file", area.getDataFile().toString()),
            Formatter.number("priority", area.getPriority()),
            Formatter.number("size", size),
            Placeholder.component("owner", owner),
            Placeholder.parsed("type", type),
            Placeholder.parsed("world", area.getWorld().key().asString())
         );
      if (area instanceof RegionizedArea<?> regionizedArea) {
         plugin.bundle()
            .sendMessage(
               sender,
               "area.info.bounds",
               Placeholder.parsed(
                  "bounds",
                  regionizedArea.getRegion().getMinimumPoint().toParserString() + " - " + regionizedArea.getRegion().getMaximumPoint().toParserString()
               )
            );
         return 1;
      } else {
         return 1;
      }
   }
}
