package net.thenextlvl.protect.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.commands.argument.AreaArgumentType;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaPriorityCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("priority")
            .requires(stack -> stack.getSender().hasPermission("protect.command.area.priority")))
         .then(
            ((RequiredArgumentBuilder)Commands.argument("area", new AreaArgumentType(plugin))
                  .then(Commands.argument("priority", IntegerArgumentType.integer()).executes(context -> set(context, plugin))))
               .executes(context -> get(context, plugin))
         );
   }

   private static int set(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      Area area = (Area)context.getArgument("area", Area.class);
      Integer priority = (Integer)context.getArgument("priority", int.class);
      String message = area.setPriority(priority) ? "area.priority.set" : "nothing.changed";
      plugin.bundle().sendMessage(sender, message, Placeholder.parsed("area", area.getName()), Formatter.number("priority", area.getPriority()));
      return 1;
   }

   private static int get(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      Area area = (Area)context.getArgument("area", Area.class);
      plugin.bundle().sendMessage(sender, "area.priority", Placeholder.parsed("area", area.getName()), Formatter.number("priority", area.getPriority()));
      return 1;
   }
}
