package net.thenextlvl.protect.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.RegionizedArea;
import net.thenextlvl.protect.commands.argument.AreaArgumentType;
import net.thenextlvl.protect.commands.argument.RegionizedAreaArgumentType;
import net.thenextlvl.protect.exception.CircularInheritanceException;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaParentCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("parent")
               .requires(stack -> stack.getSender().hasPermission("protect.command.area.parent")))
            .then(
               ((LiteralArgumentBuilder)Commands.literal("set").requires(stack -> stack.getSender().hasPermission("protect.command.area.parent.set")))
                  .then(
                     Commands.argument("area", new RegionizedAreaArgumentType(plugin))
                        .then(Commands.argument("parent", new AreaArgumentType(plugin)).executes(context -> set(context, plugin)))
                  )
            ))
         .then(
            ((LiteralArgumentBuilder)Commands.literal("remove").requires(stack -> stack.getSender().hasPermission("protect.command.area.parent.remove")))
               .then(
                  Commands.argument("area", new AreaArgumentType(plugin, (commandContext, area) -> area.getParent().isPresent()))
                     .executes(context -> remove(context, plugin))
               )
         );
   }

   private static int set(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      RegionizedArea area = (RegionizedArea)context.getArgument("area", RegionizedArea.class);
      Area parent = (Area)context.getArgument("parent", Area.class);

      try {
         CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
         boolean success = area.setParent(parent);
         String message = success ? "area.parent.set" : "nothing.changed";
         plugin.bundle().sendMessage(sender, message, Placeholder.parsed("area", area.getName()), Placeholder.parsed("parent", parent.getName()));
         return 1;
      } catch (CircularInheritanceException var7) {
         CommandSender senderx = ((CommandSourceStack)context.getSource()).getSender();
         plugin.bundle()
            .sendMessage(
               senderx, "area.parent.circular-inheritance", Placeholder.parsed("area", area.getName()), Placeholder.parsed("parent", parent.getName())
            );
         plugin.getComponentLogger().warn(var7.getMessage());
         return 0;
      }
   }

   private static int remove(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      RegionizedArea area = (RegionizedArea)context.getArgument("area", RegionizedArea.class);
      boolean success = area.setParent(null);
      String message = success ? "area.parent.remove" : "nothing.changed";
      plugin.bundle().sendMessage(sender, message, Placeholder.parsed("area", area.getName()));
      return 1;
   }
}
