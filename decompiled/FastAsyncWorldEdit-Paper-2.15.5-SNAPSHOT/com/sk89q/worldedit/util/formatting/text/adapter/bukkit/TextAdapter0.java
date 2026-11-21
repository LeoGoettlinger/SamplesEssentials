package com.sk89q.worldedit.util.formatting.text.adapter.bukkit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ImmutableList.Builder;
import com.sk89q.worldedit.util.formatting.text.Component;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class TextAdapter0 {
   private static final List<Adapter> ADAPTERS = pickAdapters();

   private static List<Adapter> pickAdapters() {
      Builder<Adapter> adapters = ImmutableList.builder();
      if (isSpigotAdapterSupported()) {
         adapters.add(new SpigotAdapter());
      }

      adapters.add(new CraftBukkitAdapter());
      adapters.add(new LegacyAdapter());
      return adapters.build();
   }

   private static boolean isSpigotAdapterSupported() {
      try {
         Class<?> spigot = Player.class.getMethod("spigot").getReturnType();
         spigot.getMethod("sendMessage", Class.forName("net.md_5.bungee.api.ChatMessageType"), Class.forName("net.md_5.bungee.api.chat.BaseComponent"));
         return true;
      } catch (ClassNotFoundException | NoSuchMethodException var1) {
         return false;
      }
   }

   static void sendComponent(final Iterable<? extends CommandSender> viewers, final Component component, final boolean actionBar) {
      List<CommandSender> list = new LinkedList<>();
      Iterables.addAll(list, viewers);
      Iterator<Adapter> it = ADAPTERS.iterator();

      while (it.hasNext() && !list.isEmpty()) {
         Adapter adapter = it.next();
         if (actionBar) {
            adapter.sendActionBar(list, component);
         } else {
            adapter.sendMessage(list, component);
         }
      }
   }
}
