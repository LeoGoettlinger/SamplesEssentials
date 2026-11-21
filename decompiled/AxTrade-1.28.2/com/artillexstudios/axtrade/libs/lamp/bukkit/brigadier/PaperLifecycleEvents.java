package com.artillexstudios.axtrade.libs.lamp.bukkit.brigadier;

import com.artillexstudios.axtrade.libs.lamp.bukkit.BukkitCommandHandler;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.lang.reflect.Method;
import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

final class PaperLifecycleEvents extends Commodore {
   private final RootCommandNode<CommandSourceStack> root = new RootCommandNode();
   private static final Method GET_LIFECYCLE_MANAGER;

   public PaperLifecycleEvents(@NotNull BukkitCommandHandler handler) {
      getLifecycleManager(handler.getPlugin()).registerEventHandler(LifecycleEvents.COMMANDS, event -> {
         for (CommandNode<CommandSourceStack> node : this.root.getChildren()) {
            ((Commands)event.registrar()).register((LiteralCommandNode)node);
         }
      });
   }

   @Override
   void register(Command command, LiteralCommandNode<?> node) {
      this.root.addChild(node);
   }

   @Override
   void register(LiteralCommandNode<?> node) {
      this.root.addChild(node);
   }

   private static LifecycleEventManager<Plugin> getLifecycleManager(Plugin plugin) {
      try {
         if (GET_LIFECYCLE_MANAGER == null) {
            throw new IllegalArgumentException("getLifecycleManager is not available.");
         } else {
            return (LifecycleEventManager<Plugin>)GET_LIFECYCLE_MANAGER.invoke(plugin);
         }
      } catch (Throwable var2) {
         throw var2;
      }
   }

   public static void ensureSetup() {
   }

   static {
      Method getLifecycleManager;
      try {
         getLifecycleManager = Plugin.class.getDeclaredMethod("getLifecycleManager");
      } catch (NoSuchMethodException var2) {
         getLifecycleManager = null;
      }

      GET_LIFECYCLE_MANAGER = getLifecycleManager;
   }
}
