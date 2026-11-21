package net.ess3.provider.providers;

import io.papermc.paper.text.PaperComponents;
import java.util.function.Consumer;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public class PaperCommandSender extends BukkitSenderProvider {
   private static final boolean FORWARDING_SENDER_AVAILABLE;

   public PaperCommandSender(ConsoleCommandSender base, BukkitSenderProvider.MessageHook hook) {
      super(base, hook);
   }

   public static boolean forwardingSenderAvailable() {
      return FORWARDING_SENDER_AVAILABLE;
   }

   public static CommandSender createCommandSender(Consumer<String> consumer) {
      return !forwardingSenderAvailable()
         ? null
         : Bukkit.createCommandSender(component -> consumer.accept(PaperComponents.legacySectionSerializer().serialize(component)));
   }

   public void sendMessage(@NotNull Identity identity, @NotNull Component message, @NotNull MessageType type) {
      this.sendDumbComponent(message);
   }

   public void sendMessage(ComponentLike message) {
      this.sendDumbComponent(message.asComponent());
   }

   public void sendMessage(@NotNull Identified source, ComponentLike message) {
      this.sendDumbComponent(message.asComponent());
   }

   public void sendMessage(@NotNull Identity source, ComponentLike message) {
      this.sendDumbComponent(message.asComponent());
   }

   public void sendMessage(@NotNull Component message) {
      this.sendDumbComponent(message);
   }

   public void sendMessage(@NotNull Identified source, @NotNull Component message) {
      this.sendDumbComponent(message);
   }

   public void sendMessage(@NotNull Identity source, @NotNull Component message) {
      this.sendDumbComponent(message);
   }

   public void sendMessage(ComponentLike message, @NotNull MessageType type) {
      this.sendDumbComponent(message.asComponent());
   }

   public void sendMessage(@NotNull Identified source, ComponentLike message, @NotNull MessageType type) {
      this.sendDumbComponent(message.asComponent());
   }

   public void sendMessage(@NotNull Identity source, ComponentLike message, @NotNull MessageType type) {
      this.sendDumbComponent(message.asComponent());
   }

   public void sendMessage(@NotNull Component message, @NotNull MessageType type) {
      this.sendDumbComponent(message);
   }

   public void sendMessage(@NotNull Identified source, @NotNull Component message, @NotNull MessageType type) {
      this.sendDumbComponent(message);
   }

   public void sendDumbComponent(Component message) {
      this.sendMessage(PaperComponents.legacySectionSerializer().serialize(message));
   }

   @NotNull
   public Component name() {
      return this.base.name();
   }

   static {
      boolean jmpHappy;
      try {
         Bukkit.createCommandSender(component -> {});
         jmpHappy = true;
      } catch (NoSuchMethodError var2) {
         jmpHappy = false;
      }

      FORWARDING_SENDER_AVAILABLE = jmpHappy;
   }
}
