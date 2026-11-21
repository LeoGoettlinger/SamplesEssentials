package com.artillexstudios.axafkzone.libs.lamp.bukkit.brigadier;

import com.artillexstudios.axafkzone.libs.lamp.bukkit.BukkitCommandHandler;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.Version;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;

final class ReflectionCommodore extends Commodore {
   private static final Field CONSOLE_FIELD;
   private static final Method GET_COMMAND_DISPATCHER_METHOD;
   private static final Method GET_BRIGADIER_DISPATCHER_METHOD;
   private final Plugin plugin;
   private final List<LiteralCommandNode<?>> registeredNodes = new ArrayList<>();

   ReflectionCommodore(BukkitCommandHandler handler) {
      this.plugin = handler.getPlugin();
      this.plugin.getServer().getPluginManager().registerEvents(new ReflectionCommodore.ServerReloadListener(), this.plugin);
   }

   private CommandDispatcher<?> getDispatcher() {
      try {
         Object mcServerObject = CONSOLE_FIELD.get(Bukkit.getServer());
         Object commandDispatcherObject = GET_COMMAND_DISPATCHER_METHOD.invoke(mcServerObject);
         return (CommandDispatcher<?>)GET_BRIGADIER_DISPATCHER_METHOD.invoke(commandDispatcherObject);
      } catch (ReflectiveOperationException var3) {
         throw new RuntimeException(var3);
      }
   }

   @Override
   public void register(LiteralCommandNode<?> node) {
      Objects.requireNonNull(node, "node");
      CommandDispatcher dispatcher = this.getDispatcher();
      RootCommandNode root = dispatcher.getRoot();
      BrigadierUtil.removeChild(root, node.getName());
      root.addChild(node);
      this.registeredNodes.add(node);
   }

   @Override
   public void register(Command command, LiteralCommandNode<?> node) {
      Objects.requireNonNull(command, "command");
      Objects.requireNonNull(node, "node");
      Collection<String> aliases = getAliases(command);
      if (!aliases.contains(node.getLiteral())) {
         node = BrigadierUtil.renameLiteralNode(node, command.getName());
      }

      for (String alias : aliases) {
         if (node.getLiteral().equals(alias)) {
            this.register(node);
         } else {
            this.register(((LiteralArgumentBuilder)LiteralArgumentBuilder.literal(alias).redirect(node)).build());
         }
      }

      this.plugin.getServer().getPluginManager().registerEvents(new ReflectionCommodore.CommandDataSendListener(command), this.plugin);
   }

   static void ensureSetup() {
   }

   static {
      try {
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_19)) {
            throw new UnsupportedOperationException("ReflectionCommodore is not supported on MC 1.19 or above. Switch to Paper :)");
         } else {
            Class<?> minecraftServer;
            Class<?> commandDispatcher;
            if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_16_5)) {
               minecraftServer = BukkitVersion.findNmsClass("server.MinecraftServer");
               commandDispatcher = BukkitVersion.findNmsClass("commands.CommandDispatcher");
            } else {
               minecraftServer = BukkitVersion.findNmsClass("MinecraftServer");
               commandDispatcher = BukkitVersion.findNmsClass("CommandDispatcher");
            }

            Class<?> craftServer = BukkitVersion.findOcbClass("CraftServer");
            CONSOLE_FIELD = craftServer.getDeclaredField("console");
            CONSOLE_FIELD.setAccessible(true);
            GET_COMMAND_DISPATCHER_METHOD = Arrays.stream(minecraftServer.getDeclaredMethods())
               .filter(method -> method.getParameterCount() == 0)
               .filter(method -> commandDispatcher.isAssignableFrom(method.getReturnType()))
               .findFirst()
               .orElseThrow(NoSuchMethodException::new);
            GET_COMMAND_DISPATCHER_METHOD.setAccessible(true);
            GET_BRIGADIER_DISPATCHER_METHOD = Arrays.stream(commandDispatcher.getDeclaredMethods())
               .filter(method -> method.getParameterCount() == 0)
               .filter(method -> CommandDispatcher.class.isAssignableFrom(method.getReturnType()))
               .findFirst()
               .orElseThrow(NoSuchMethodException::new);
            GET_BRIGADIER_DISPATCHER_METHOD.setAccessible(true);
         }
      } catch (ReflectiveOperationException var3) {
         throw new ExceptionInInitializerError(var3);
      }
   }

   private static final class CommandDataSendListener implements Listener {
      private final Set<String> minecraftPrefixedAliases;

      CommandDataSendListener(Command pluginCommand) {
         this.minecraftPrefixedAliases = Commodore.getAliases(pluginCommand).stream().map(alias -> "minecraft:" + alias).collect(Collectors.toSet());
      }

      @EventHandler
      public void onCommandSend(PlayerCommandSendEvent e) {
         e.getCommands().removeAll(this.minecraftPrefixedAliases);
      }
   }

   private final class ServerReloadListener implements Listener {
      private ServerReloadListener() {
      }

      @EventHandler
      public void onLoad(ServerLoadEvent e) {
         CommandDispatcher dispatcher = ReflectionCommodore.this.getDispatcher();
         RootCommandNode root = dispatcher.getRoot();

         for (LiteralCommandNode<?> node : ReflectionCommodore.this.registeredNodes) {
            BrigadierUtil.removeChild(root, node.getName());
            root.addChild(node);
         }
      }

      @EventHandler
      public void onPluginDisable(PluginDisableEvent e) {
         if (ReflectionCommodore.this.plugin == e.getPlugin()) {
            CommandDispatcher dispatcher = ReflectionCommodore.this.getDispatcher();
            RootCommandNode root = dispatcher.getRoot();

            for (LiteralCommandNode<?> node : ReflectionCommodore.this.registeredNodes) {
               BrigadierUtil.removeChild(root, node.getName());
            }
         }
      }
   }
}
