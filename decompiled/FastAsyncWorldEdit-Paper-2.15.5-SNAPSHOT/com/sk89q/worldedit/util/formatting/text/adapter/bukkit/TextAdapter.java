package com.sk89q.worldedit.util.formatting.text.adapter.bukkit;

import com.sk89q.worldedit.util.formatting.text.Component;
import java.util.Collections;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface TextAdapter {
   static void sendMessage(@NonNull final CommandSender viewer, @NonNull final Component component) {
      sendComponent(Collections.singleton(viewer), component);
   }

   static void sendMessage(@NonNull final Iterable<? extends CommandSender> viewers, @NonNull final Component component) {
      TextAdapter0.sendComponent(viewers, component, false);
   }

   @Deprecated
   static void sendComponent(@NonNull final CommandSender viewer, @NonNull final Component component) {
      sendComponent(Collections.singleton(viewer), component);
   }

   @Deprecated
   static void sendComponent(@NonNull final Iterable<? extends CommandSender> viewers, @NonNull final Component component) {
      TextAdapter0.sendComponent(viewers, component, false);
   }

   static void sendActionBar(@NonNull final CommandSender viewer, @NonNull final Component component) {
      sendActionBar(Collections.singleton(viewer), component);
   }

   static void sendActionBar(@NonNull final Iterable<? extends CommandSender> viewers, @NonNull final Component component) {
      TextAdapter0.sendComponent(viewers, component, true);
   }
}
