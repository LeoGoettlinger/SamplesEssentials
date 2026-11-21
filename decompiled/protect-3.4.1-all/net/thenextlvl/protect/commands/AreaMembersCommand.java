package net.thenextlvl.protect.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.commands.argument.AreaArgumentType;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaMembersCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("members")
                  .requires(stack -> stack.getSender().hasPermission("protect.command.area.members")))
               .then(
                  ((LiteralArgumentBuilder)Commands.literal("add").requires(stack -> stack.getSender().hasPermission("protect.command.area.members.add")))
                     .then(
                        Commands.argument("area", new AreaArgumentType(plugin))
                           .then(Commands.argument("players", ArgumentTypes.players()).executes(context -> add(context, plugin)))
                     )
               ))
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("list")
                        .requires(stack -> stack.getSender().hasPermission("protect.command.area.members.list")))
                     .then(Commands.argument("area", new AreaArgumentType(plugin)).executes(context -> {
                        Area area = (Area)context.getArgument("area", Area.class);
                        return list(context, area, plugin);
                     })))
                  .executes(context -> {
                     Area area = plugin.areaProvider().getArea(((CommandSourceStack)context.getSource()).getLocation());
                     return list(context, area, plugin);
                  })
            ))
         .then(
            ((LiteralArgumentBuilder)Commands.literal("remove").requires(stack -> stack.getSender().hasPermission("protect.command.area.members.remove")))
               .then(
                  Commands.argument("area", new AreaArgumentType(plugin, (commandContext, area) -> !area.getMembers().isEmpty()))
                     .then(
                        Commands.argument("player", StringArgumentType.word())
                           .suggests(
                              (context, builder) -> {
                                 Area area = (Area)context.getLastChild().getArgument("area", Area.class);
                                 return CompletableFuture.runAsync(
                                       () -> area.getMembers()
                                          .stream()
                                          .map(plugin.getServer()::getOfflinePlayer)
                                          .<String>map(OfflinePlayer::getName)
                                          .filter(Objects::nonNull)
                                          .forEach(builder::suggest)
                                    )
                                    .thenApply(unused -> builder.build());
                              }
                           )
                           .executes(context -> remove(context, plugin))
                     )
               )
         );
   }

   private static int list(CommandContext<CommandSourceStack> context, Area area, ProtectPlugin plugin) {
      plugin.getServer()
         .getAsyncScheduler()
         .runNow(
            plugin,
            task -> {
               CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
               List<TextComponent> members = area.getMembers()
                  .stream()
                  .map(plugin.getServer()::getOfflinePlayer)
                  .<String>map(OfflinePlayer::getName)
                  .filter(Objects::nonNull)
                  .<TextComponent>map(Component::text)
                  .toList();
               String message = members.isEmpty() ? "area.members.none" : "area.members";
               plugin.bundle()
                  .sendMessage(
                     sender,
                     message,
                     Placeholder.parsed("area", area.getName()),
                     Formatter.number("amount", members.size()),
                     Formatter.joining("members", members)
                  );
            }
         );
      return 1;
   }

   private static int add(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) throws CommandSyntaxException {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      Area area = (Area)context.getArgument("area", Area.class);
      PlayerSelectorArgumentResolver resolver = (PlayerSelectorArgumentResolver)context.getArgument("players", PlayerSelectorArgumentResolver.class);
      List<Player> resolve = (List<Player>)resolver.resolve((CommandSourceStack)context.getSource());
      List<Player> added = resolve.stream().filter(player -> area.addMember(player.getUniqueId())).toList();
      if (added.isEmpty()) {
         plugin.bundle().sendMessage(sender, "nothing.changed");
      } else {
         added.forEach(
            player -> plugin.bundle()
               .sendMessage(
                  sender,
                  "area.members.add",
                  Placeholder.component("player", player.name().hoverEvent(player.asHoverEvent())),
                  Placeholder.parsed("area", area.getName())
               )
         );
      }

      return added.size();
   }

   private static int remove(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      plugin.getServer()
         .getAsyncScheduler()
         .runNow(
            plugin,
            task -> {
               CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
               Area area = (Area)context.getArgument("area", Area.class);
               String name = (String)context.getArgument("player", String.class);
               OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
               boolean success = area.removeMember(player.getUniqueId());
               String message = success ? "area.members.remove" : "nothing.changed";
               plugin.bundle()
                  .sendMessage(
                     sender,
                     message,
                     Placeholder.parsed("area", area.getName()),
                     Placeholder.parsed("player", player.getName() != null ? player.getName() : name)
                  );
            }
         );
      return 1;
   }
}
