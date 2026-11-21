package net.thenextlvl.protect.commands;

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
import net.thenextlvl.protect.flag.ProtectionFlag;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaProtectCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("protect")
            .requires(stack -> stack.getSender().hasPermission("protect.command.area.protect")))
         .then(
            ((RequiredArgumentBuilder)Commands.argument("area", new AreaArgumentType(plugin))
                  .then(Commands.literal("remove").executes(context -> unprotect(context, plugin))))
               .executes(context -> protect(context, plugin))
         );
   }

   private static int unprotect(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      Area area = (Area)context.getArgument("area", Area.class);
      long changes = plugin.flagRegistry().getFlags().stream().filter(flag -> flag instanceof ProtectionFlag).filter(area::removeFlag).count();
      String message = changes > 0L ? "area.unprotected" : "nothing.changed";
      plugin.bundle()
         .sendMessage(
            ((CommandSourceStack)context.getSource()).getSender(), message, Formatter.number("amount", changes), Placeholder.parsed("area", area.getName())
         );
      return 1;
   }

   private static int protect(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      Area area = (Area)context.getArgument("area", Area.class);
      long changes = plugin.flagRegistry()
         .getFlags()
         .stream()
         .filter(flag -> flag instanceof ProtectionFlag)
         .map(flag -> (ProtectionFlag)flag)
         .filter(flag -> area.setFlag(flag, flag.protectedValue()))
         .count();
      String message = changes > 0L ? "area.protected" : "nothing.changed";
      plugin.bundle()
         .sendMessage(
            ((CommandSourceStack)context.getSource()).getSender(), message, Formatter.number("amount", changes), Placeholder.parsed("area", area.getName())
         );
      return 1;
   }
}
