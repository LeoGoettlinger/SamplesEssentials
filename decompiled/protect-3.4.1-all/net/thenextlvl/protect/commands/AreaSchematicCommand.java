package net.thenextlvl.protect.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.sk89q.worldedit.WorldEditException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.io.IOException;
import java.nio.file.Files;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.RegionizedArea;
import net.thenextlvl.protect.commands.argument.RegionizedAreaArgumentType;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaSchematicCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                     "schematic"
                  )
                  .requires(stack -> stack.getSender().hasPermission("protect.command.area.schematic")))
               .then(delete(plugin)))
            .then(load(plugin)))
         .then(save(plugin));
   }

   private static ArgumentBuilder<CommandSourceStack, ?> delete(ProtectPlugin plugin) {
      return ((LiteralArgumentBuilder)Commands.literal("delete").requires(stack -> stack.getSender().hasPermission("protect.command.area.schematic.delete")))
         .then(
            Commands.argument("area", new RegionizedAreaArgumentType(plugin, area -> Files.isRegularFile(area.getSchematicFile())))
               .executes(context -> delete(context, plugin))
         );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> load(ProtectPlugin plugin) {
      return ((LiteralArgumentBuilder)Commands.literal("load").requires(stack -> stack.getSender().hasPermission("protect.command.area.schematic.load")))
         .then(
            Commands.argument("area", new RegionizedAreaArgumentType(plugin, area -> Files.isRegularFile(area.getSchematicFile())))
               .executes(context -> load(context, plugin))
         );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> save(ProtectPlugin plugin) {
      return ((LiteralArgumentBuilder)Commands.literal("save").requires(stack -> stack.getSender().hasPermission("protect.command.area.schematic.save")))
         .then(Commands.argument("area", new RegionizedAreaArgumentType(plugin)).executes(context -> save(context, plugin)));
   }

   private static int delete(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      RegionizedArea area = (RegionizedArea)context.getArgument("area", RegionizedArea.class);
      String message = area.deleteSchematic() ? "area.schematic.delete.success" : "area.schematic.delete.failed";
      plugin.bundle().sendMessage(sender, message, Placeholder.parsed("schematic", area.getSchematicFile().getFileName().toString()));
      return 1;
   }

   private static int load(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      RegionizedArea area = (RegionizedArea)context.getArgument("area", RegionizedArea.class);
      if (area.isTooBig()) {
         plugin.bundle().sendMessage(sender, "area.warning.size");
      }

      boolean load = false;

      try {
         load = area.loadSchematic();
      } catch (WorldEditException | IOException var6) {
         plugin.getComponentLogger().error("Failed to load area schematic", var6);
         plugin.getComponentLogger()
            .error(
               "Please look for similar issues or report this on GitHub: {}", "https://github.com/TheNextLvl-net/protect/issues/new?template=bug_report.yml"
            );
         ProtectPlugin.ERROR_TRACKER.trackError(var6);
      }

      String message = load ? "area.schematic.load.success" : "area.schematic.load.failed";
      plugin.bundle().sendMessage(sender, message, Placeholder.parsed("schematic", area.getSchematicFile().getFileName().toString()));
      return 1;
   }

   private static int save(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      RegionizedArea area = (RegionizedArea)context.getArgument("area", RegionizedArea.class);
      if (area.isTooBig()) {
         plugin.bundle().sendMessage(sender, "area.warning.size");
      }

      try {
         area.saveSchematic();
         long size = 0L;

         try {
            size = Files.size(area.getSchematicFile()) / 1024L;
         } catch (IOException var7) {
            plugin.getComponentLogger().warn("Failed to get file size of area {}", area.getName(), var7);
         }

         plugin.bundle()
            .sendMessage(
               sender,
               "area.schematic.save.success",
               Placeholder.parsed("schematic", area.getSchematicFile().getFileName().toString()),
               Formatter.number("size", size)
            );
      } catch (WorldEditException | IOException var8) {
         plugin.bundle().sendMessage(sender, "area.schematic.save.failed", Placeholder.parsed("schematic", area.getSchematicFile().getFileName().toString()));
         plugin.getComponentLogger().error("Failed to save area schematic", var8);
         plugin.getComponentLogger()
            .error(
               "Please look for similar issues or report this on GitHub: {}", "https://github.com/TheNextLvl-net/protect/issues/new?template=bug_report.yml"
            );
         ProtectPlugin.ERROR_TRACKER.trackError(var8);
      }

      return 1;
   }
}
