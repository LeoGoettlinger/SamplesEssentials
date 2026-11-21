package com.artillexstudios.axafkzone.libs.lamp.bukkit.brigadier;

import com.artillexstudios.axafkzone.libs.lamp.bukkit.BukkitCommandActor;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.BukkitCommandHandler;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.core.BukkitCommandExecutor;
import com.artillexstudios.axafkzone.libs.lamp.command.ArgumentStack;
import com.artillexstudios.axafkzone.libs.lamp.exception.ArgumentParseException;
import com.artillexstudios.axafkzone.libs.lamp.util.Strings;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Warning.WarningState;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.command.UnknownCommandEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

final class PaperCommodore extends Commodore implements Listener {
   private final Map<String, LiteralCommandNode<?>> commands = new HashMap<>();
   private final BukkitCommandHandler handler;
   private final String fallbackPrefix;

   PaperCommodore(@NotNull BukkitCommandHandler handler) {
      this.handler = handler;
      Plugin plugin = handler.getPlugin();
      this.fallbackPrefix = plugin.getName().toLowerCase().trim();
      this.registerListener(plugin);
   }

   private void registerListener(Plugin plugin) {
      Bukkit.getPluginManager().registerEvents(new PaperCommodore.UnknownCommandListener(), plugin);

      try {
         WarningState prevState = Bukkit.getWarningState();
         Class<?> craftServer = Bukkit.getServer().getClass();
         Field warningState = craftServer.getDeclaredField("warningState");
         warningState.setAccessible(true);
         warningState.set(Bukkit.getServer(), WarningState.OFF);
         Bukkit.getPluginManager().registerEvents(new PaperCommodore.CommandRegisterListener(), plugin);
         warningState.set(Bukkit.getServer(), prevState);
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   @Override
   public void register(LiteralCommandNode<?> node) {
      Objects.requireNonNull(node, "node");
      this.commands.put(node.getLiteral(), node);
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
            this.commands.put(node.getLiteral(), node);
         } else {
            LiteralCommandNode<Object> redirectNode = ((LiteralArgumentBuilder)LiteralArgumentBuilder.literal(alias).redirect(node)).build();
            this.commands.put(redirectNode.getLiteral(), redirectNode);
         }
      }
   }

   static void ensureSetup() {
   }

   static {
      try {
         Class.forName("com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent");
      } catch (ClassNotFoundException var1) {
         throw new UnsupportedOperationException("Not running on modern Paper!", var1);
      }
   }

   public final class CommandRegisterListener implements Listener {
      @EventHandler
      public void onCommandRegistered(CommandRegisteredEvent<?> event) {
         if (event.getCommand() instanceof PluginCommand) {
            PluginCommand pCommand = (PluginCommand)event.getCommand();
            if (pCommand.getExecutor() instanceof BukkitCommandExecutor) {
               LiteralCommandNode<?> node = PaperCommodore.this.commands.get(event.getCommandLabel());
               if (node != null) {
                  event.setLiteral(node);
               }
            }
         }
      }
   }

   public final class UnknownCommandListener implements Listener {
      @EventHandler
      public void onUnknownCommand(UnknownCommandEvent event) {
         if (!event.getCommandLine().isEmpty()) {
            try {
               ArgumentStack args = ArgumentStack.parse(Strings.stripNamespace(PaperCommodore.this.fallbackPrefix, event.getCommandLine()));
               if (PaperCommodore.this.commands.containsKey(args.getFirst())) {
                  event.setMessage(null);
                  BukkitCommandActor actor = BukkitCommandActor.wrap(event.getSender(), PaperCommodore.this.handler);

                  try {
                     PaperCommodore.this.handler.dispatch(actor, args);
                  } catch (Throwable var5) {
                     PaperCommodore.this.handler.getExceptionHandler().handleException(var5, actor);
                  }
               }
            } catch (ArgumentParseException var6) {
            }
         }
      }
   }
}
