package net.thenextlvl.protect.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.nio.file.Files;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.RegionizedArea;
import net.thenextlvl.protect.commands.argument.RegionizedAreaArgumentType;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaDeleteCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("delete")
            .requires(stack -> stack.getSender().hasPermission("protect.command.area.delete")))
         .then(Commands.argument("area", new RegionizedAreaArgumentType(plugin)).executes(context -> execute(context, plugin)));
   }

   private static int execute(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      RegionizedArea<?> area = (RegionizedArea<?>)context.getArgument("area", RegionizedArea.class);
      String message = Files.isRegularFile(area.getSchematicFile())
         ? "area.schematic.delete"
         : (plugin.areaService().delete(area) ? "area.delete.success" : "area.delete.failed");
      plugin.bundle().sendMessage(sender, message, Placeholder.parsed("area", area.getName()));
      return 1;
   }
}
