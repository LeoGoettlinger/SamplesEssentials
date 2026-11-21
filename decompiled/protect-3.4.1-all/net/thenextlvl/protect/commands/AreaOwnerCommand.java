package net.thenextlvl.protect.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import java.util.List;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.commands.argument.AreaArgumentType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaOwnerCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("owner")
               .requires(stack -> stack.getSender().hasPermission("protect.command.area.owner")))
            .then(
               ((LiteralArgumentBuilder)Commands.literal("set").requires(stack -> stack.getSender().hasPermission("protect.command.area.owner.set")))
                  .then(
                     Commands.argument("area", new AreaArgumentType(plugin))
                        .then(Commands.argument("player", ArgumentTypes.player()).executes(context -> set(context, plugin)))
                  )
            ))
         .then(
            ((LiteralArgumentBuilder)Commands.literal("remove").requires(stack -> stack.getSender().hasPermission("protect.command.area.owner.remove")))
               .then(
                  Commands.argument("area", new AreaArgumentType(plugin, (commandContext, area) -> area.getOwner().isPresent()))
                     .executes(context -> remove(context, plugin))
               )
         );
   }

   private static int set(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) throws CommandSyntaxException {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      Area area = (Area)context.getArgument("area", Area.class);
      PlayerSelectorArgumentResolver resolver = (PlayerSelectorArgumentResolver)context.getArgument("player", PlayerSelectorArgumentResolver.class);
      Player player = (Player)((List)resolver.resolve((CommandSourceStack)context.getSource())).getFirst();
      boolean success = area.setOwner(player.getUniqueId());
      String message = success ? "area.owner.set" : "nothing.changed";
      plugin.bundle()
         .sendMessage(
            sender, message, Placeholder.component("player", player.name().hoverEvent(player.asHoverEvent())), Placeholder.parsed("area", area.getName())
         );
      return 1;
   }

   private static int remove(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      Area area = (Area)context.getArgument("area", Area.class);
      boolean success = area.setOwner(null);
      String message = success ? "area.owner.remove" : "nothing.changed";
      plugin.bundle().sendMessage(sender, message, Placeholder.parsed("area", area.getName()));
      return 1;
   }
}
