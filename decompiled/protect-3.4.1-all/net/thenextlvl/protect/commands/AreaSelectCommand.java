package net.thenextlvl.protect.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.RegionizedArea;
import net.thenextlvl.protect.commands.argument.RegionizedAreaArgumentType;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaSelectCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("select")
            .requires(stack -> stack.getSender() instanceof Player player && player.hasPermission("worldedit.selection.pos")))
         .then(Commands.argument("area", new RegionizedAreaArgumentType(plugin)).executes(context -> select(context, plugin)));
   }

   private static int select(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      Player player = (Player)((CommandSourceStack)context.getSource()).getSender();
      RegionizedArea area = (RegionizedArea)context.getArgument("area", RegionizedArea.class);
      LocalSession session = WorldEditPlugin.getInstance().getSession(player);
      session.setRegionSelector(BukkitAdapter.adapt(area.getWorld()), area.getRegionSelector());
      session.updateServerCUI(BukkitAdapter.adapt(player));
      plugin.bundle().sendMessage(player, "area.select", Placeholder.parsed("area", area.getName()));
      return 1;
   }
}
