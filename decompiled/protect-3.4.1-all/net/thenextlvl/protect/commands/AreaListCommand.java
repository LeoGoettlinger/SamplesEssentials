package net.thenextlvl.protect.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.GlobalArea;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaListCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("list")
            .requires(stack -> stack.getSender().hasPermission("protect.command.area.list")))
         .executes(context -> list(context, plugin));
   }

   private static int list(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      List<TextComponent> globalAreas = plugin.areaProvider()
         .getAreas()
         .filter(GlobalArea.class::isInstance)
         .map(Area::getName)
         .<TextComponent>map(Component::text)
         .toList();
      List<TextComponent> userAreas = plugin.areaProvider()
         .getAreas()
         .filter(area -> !(area instanceof GlobalArea))
         .map(Area::getName)
         .<TextComponent>map(Component::text)
         .toList();
      if (!userAreas.isEmpty()) {
         plugin.bundle().sendMessage(sender, "area.list", Formatter.number("amount", userAreas.size()), Formatter.joining("areas", userAreas));
      }

      plugin.bundle().sendMessage(sender, "area.list.global", Formatter.number("amount", globalAreas.size()), Formatter.joining("areas", globalAreas));
      return 1;
   }
}
