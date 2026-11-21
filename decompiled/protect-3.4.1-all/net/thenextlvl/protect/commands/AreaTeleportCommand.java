package net.thenextlvl.protect.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.RegionizedArea;
import net.thenextlvl.protect.commands.argument.RegionizedAreaArgumentType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaTeleportCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("teleport")
            .requires(stack -> stack.getSender() instanceof Player player && player.hasPermission("protect.command.area.teleport")))
         .then(Commands.argument("area", new RegionizedAreaArgumentType(plugin)).executes(context -> teleport(context, plugin)));
   }

   private static int teleport(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      Player player = (Player)((CommandSourceStack)context.getSource()).getSender();
      RegionizedArea area = (RegionizedArea)context.getArgument("area", RegionizedArea.class);
      player.teleportAsync(area.getLocation(), TeleportCause.COMMAND).thenAccept(success -> {
         String message = success ? "area.teleport.success" : "area.teleport.fail";
         plugin.bundle().sendMessage(player, message, Placeholder.parsed("area", area.getName()));
      });
      return 1;
   }
}
