package net.thenextlvl.protect.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.GroupedArea;
import net.thenextlvl.protect.area.RegionizedArea;
import net.thenextlvl.protect.commands.argument.GroupedAreaArgumentType;
import net.thenextlvl.protect.commands.argument.RegionizedAreaArgumentType;
import net.thenextlvl.protect.region.GroupedRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaGroupCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                 "group"
                              )
                              .requires(stack -> stack.getSender().hasPermission("protect.command.area.group")))
                           .then(add(plugin)))
                        .then(delete(plugin)))
                     .then(groupCreate(plugin)))
                  .then(list(plugin)))
               .then(redefine(plugin)))
            .then(remove(plugin)))
         .then(select(plugin));
   }

   private static LiteralArgumentBuilder<CommandSourceStack> add(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("add")
            .requires(stack -> stack.getSender().hasPermission("protect.command.area.group.add")))
         .then(
            ((RequiredArgumentBuilder)Commands.argument("group", new GroupedAreaArgumentType(plugin))
                  .then(Commands.argument("name", StringArgumentType.string()).executes(context -> {
                     String name = (String)context.getArgument("name", String.class);
                     return add(context, name, plugin);
                  })))
               .executes(context -> {
                  GroupedArea group = (GroupedArea)context.getArgument("group", GroupedArea.class);
                  String name = "region-" + (group.getRegion().getRegions().size() + 1);
                  return add(context, name, plugin);
               })
         );
   }

   private static LiteralArgumentBuilder<CommandSourceStack> delete(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("delete")
            .requires(stack -> stack.getSender().hasPermission("protect.command.area.group.delete")))
         .then(Commands.argument("group", new GroupedAreaArgumentType(plugin)).executes(context -> delete(context, plugin)));
   }

   private static LiteralArgumentBuilder<CommandSourceStack> groupCreate(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("create")
            .requires(stack -> stack.getSender().hasPermission("protect.command.area.group.create")))
         .then(
            Commands.argument("area", new RegionizedAreaArgumentType(plugin, regionizedArea -> !(regionizedArea instanceof GroupedArea)))
               .executes(context -> create(context, plugin))
         );
   }

   private static LiteralArgumentBuilder<CommandSourceStack> list(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("list")
               .requires(stack -> stack.getSender().hasPermission("protect.command.area.group.list")))
            .then(Commands.argument("group", new GroupedAreaArgumentType(plugin)).executes(context -> list(context, plugin))))
         .executes(context -> listAll(context, plugin));
   }

   private static LiteralArgumentBuilder<CommandSourceStack> redefine(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("redefine")
            .requires(stack -> stack.getSender() instanceof Player player && player.hasPermission("protect.command.area.group.redefine")))
         .then(
            Commands.argument("group", new GroupedAreaArgumentType(plugin))
               .then(Commands.argument("region", StringArgumentType.string()).suggests(suggestGroups(plugin)).executes(context -> redefine(context, plugin)))
         );
   }

   private static LiteralArgumentBuilder<CommandSourceStack> remove(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("remove")
            .requires(stack -> stack.getSender().hasPermission("protect.command.area.group.remove")))
         .then(
            Commands.argument("group", new GroupedAreaArgumentType(plugin))
               .then(Commands.argument("region", StringArgumentType.string()).suggests(suggestGroups(plugin)).executes(context -> remove(context, plugin)))
         );
   }

   private static LiteralArgumentBuilder<CommandSourceStack> select(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("select")
            .requires(stack -> stack.getSender().hasPermission("worldedit.selection.pos")))
         .then(
            Commands.argument("group", new GroupedAreaArgumentType(plugin))
               .then(Commands.argument("region", StringArgumentType.string()).suggests(suggestGroups(plugin)).executes(context -> select(context, plugin)))
         );
   }

   private static SuggestionProvider<CommandSourceStack> suggestGroups(ProtectPlugin plugin) {
      return (context, builder) -> {
         GroupedArea group = (GroupedArea)context.getLastChild().getArgument("group", GroupedArea.class);
         group.getRegion().getRegions().keySet().forEach(builder::suggest);
         return builder.buildFuture();
      };
   }

   private static int remove(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      GroupedArea group = (GroupedArea)context.getArgument("group", GroupedArea.class);
      String region = (String)context.getArgument("region", String.class);
      boolean success = group.getRegion().removeRegion(region);
      String message = success ? "area.group.remove" : "nothing.changed";
      plugin.bundle().sendMessage(sender, message, Placeholder.parsed("area", group.getName()), Placeholder.parsed("region", region));
      return success ? 1 : 0;
   }

   private static int list(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      GroupedArea group = (GroupedArea)context.getArgument("group", GroupedArea.class);
      List<TextComponent> regions = group.getRegion().getRegions().keySet().stream().<TextComponent>map(Component::text).toList();
      plugin.bundle()
         .sendMessage(
            sender,
            "area.list.regions",
            Formatter.number("amount", regions.size()),
            Placeholder.parsed("group", group.getName()),
            Formatter.joining("regions", regions)
         );
      return 1;
   }

   private static int listAll(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      List<TextComponent> groups = plugin.areaProvider()
         .getAreas()
         .filter(GroupedArea.class::isInstance)
         .map(Area::getName)
         .<TextComponent>map(Component::text)
         .toList();
      plugin.bundle().sendMessage(sender, "area.list.groups", Formatter.number("amount", groups.size()), Formatter.joining("groups", groups));
      return 1;
   }

   private static int redefine(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      Player player = (Player)((CommandSourceStack)context.getSource()).getSender();
      GroupedArea group = (GroupedArea)context.getArgument("group", GroupedArea.class);
      String name = (String)context.getArgument("region", String.class);

      try {
         WorldEditPlugin worldEdit = (WorldEditPlugin)JavaPlugin.getPlugin(WorldEditPlugin.class);
         Region region = worldEdit.getSession(player).getSelection();
         boolean redefine = region.getClass().isInstance(group.getRegion().getRegion(name));
         String message = redefine ? "area.redefine.success.group" : "area.redefine.unsupported";
         if (redefine) {
            group.getRegion().setRegion(name, region);
         }

         plugin.bundle()
            .sendMessage(
               player,
               message,
               Placeholder.parsed("group", group.getName()),
               Placeholder.parsed("region", name),
               Placeholder.parsed("type", region.getClass().getSimpleName())
            );
         return redefine ? 1 : 0;
      } catch (IncompleteRegionException var9) {
         plugin.bundle().sendMessage(player, "region.select");
         return 0;
      }
   }

   private static int add(CommandContext<CommandSourceStack> context, String name, ProtectPlugin plugin) {
      Player player = (Player)((CommandSourceStack)context.getSource()).getSender();

      try {
         LocalSession session = WorldEditPlugin.getInstance().getSession(player);
         Region region = session.getSelection();
         GroupedArea group = (GroupedArea)context.getArgument("group", GroupedArea.class);
         if (!group.getRegion().getRegions().values().iterator().next().getClass().isInstance(region)) {
            plugin.bundle().sendMessage(player, "region.unsupported", Placeholder.parsed("type", region.getClass().getSimpleName()));
            return 0;
         } else {
            boolean success = !group.getRegion().hasRegion(region) && group.getRegion().addRegion(name, region);
            String message = success ? "area.group.add" : "nothing.changed";
            plugin.bundle().sendMessage(player, message, Placeholder.parsed("area", group.getName()), Placeholder.parsed("region", name));
            return success ? 1 : 0;
         }
      } catch (IncompleteRegionException var9) {
         plugin.bundle().sendMessage(player, "region.select");
         return 0;
      }
   }

   private static int delete(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      GroupedArea group = (GroupedArea)context.getArgument("group", GroupedArea.class);
      if (!plugin.areaService().delete(group)) {
         plugin.bundle().sendMessage(sender, "area.group.delete.failed", Placeholder.parsed("group", group.getName()));
         return 0;
      } else {
         try {
            Region region = group.getRegion().getRegions().values().iterator().next();
            RegionizedArea<Region> area = plugin.areaService().creator(group).region(region).create();
            plugin.bundle().sendMessage(sender, "area.group.delete", Placeholder.parsed("group", area.getName()));
            return 1;
         } catch (Exception var6) {
            plugin.getComponentLogger().error("Failed to create grouped area", var6);
            if (sender instanceof ConsoleCommandSender) {
               return 0;
            } else {
               plugin.bundle().sendMessage(sender, "command.error");
               return 0;
            }
         }
      }
   }

   private static int create(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      RegionizedArea area = (RegionizedArea)context.getArgument("area", RegionizedArea.class);
      if (!plugin.areaService().delete(area)) {
         plugin.bundle().sendMessage(sender, "area.group.create.failed", Placeholder.parsed("area", area.getName()));
         return 0;
      } else {
         try {
            RegionizedArea<GroupedRegion> grouped = plugin.areaService().creator(area).region(new GroupedRegion(Map.of("root", area.getRegion()))).create();
            plugin.bundle().sendMessage(sender, "area.group.create", Placeholder.parsed("area", grouped.getName()));
            return 1;
         } catch (Exception var5) {
            plugin.getComponentLogger().error("Failed to create grouped area", var5);
            if (sender instanceof ConsoleCommandSender) {
               return 0;
            } else {
               plugin.bundle().sendMessage(sender, "command.error");
               return 0;
            }
         }
      }
   }

   private static int select(CommandContext<CommandSourceStack> context, ProtectPlugin plugin) {
      Player player = (Player)((CommandSourceStack)context.getSource()).getSender();
      GroupedArea group = (GroupedArea)context.getArgument("group", GroupedArea.class);
      String name = (String)context.getArgument("region", String.class);
      Region region = group.getRegion().getRegion(name);
      if (region == null) {
         plugin.bundle().sendMessage(player, "area.group.region.unknown", Placeholder.parsed("group", group.getName()), Placeholder.parsed("region", name));
         return 0;
      } else {
         LocalSession session = WorldEditPlugin.getInstance().getSession(player);
         CuboidRegionSelector selector = new CuboidRegionSelector(null, region.getMinimumPoint(), region.getMaximumPoint());
         session.setRegionSelector(BukkitAdapter.adapt(group.getWorld()), selector);
         session.updateServerCUI(BukkitAdapter.adapt(player));
         plugin.bundle().sendMessage(player, "area.select.group", Placeholder.parsed("group", group.getName()), Placeholder.parsed("region", name));
         return 1;
      }
   }
}
