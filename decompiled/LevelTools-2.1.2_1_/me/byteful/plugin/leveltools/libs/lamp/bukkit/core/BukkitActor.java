package me.byteful.plugin.leveltools.libs.lamp.bukkit.core;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import me.byteful.plugin.leveltools.libs.lamp.CommandHandler;
import me.byteful.plugin.leveltools.libs.lamp.bukkit.BukkitCommandActor;
import me.byteful.plugin.leveltools.libs.lamp.bukkit.BukkitCommandHandler;
import me.byteful.plugin.leveltools.libs.lamp.bukkit.exception.SenderNotConsoleException;
import me.byteful.plugin.leveltools.libs.lamp.bukkit.exception.SenderNotPlayerException;
import me.byteful.plugin.leveltools.libs.lamp.core.reflect.MethodCaller;
import me.byteful.plugin.leveltools.libs.lamp.core.reflect.MethodCallerFactory;
import me.byteful.plugin.leveltools.libs.lamp.locales.Locales;
import me.byteful.plugin.leveltools.libs.lamp.util.Preconditions;
import me.byteful.plugin.leveltools.libs.lamp.util.Strings;
import me.byteful.plugin.leveltools.libs.lamp.util.Suppliers;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class BukkitActor implements BukkitCommandActor {
   private static final UUID CONSOLE_UUID = UUID.nameUUIDFromBytes("CONSOLE".getBytes(StandardCharsets.UTF_8));
   private final CommandSender sender;
   private final BukkitHandler handler;
   private static final Supplier<Optional<MethodCaller>> getLocale = Suppliers.lazy(() -> {
      try {
         Method spigotGetLocale = Spigot.class.getDeclaredMethod("getLocale");
         MethodCaller caller = MethodCallerFactory.defaultFactory().createFor(spigotGetLocale);
         return Optional.of(caller);
      } catch (Throwable var2) {
         return Optional.empty();
      }
   });

   public BukkitActor(CommandSender sender, CommandHandler handler) {
      this.sender = Preconditions.notNull(sender, "sender");
      this.handler = Preconditions.notNull(handler, "handler");
   }

   @NotNull
   @Override
   public CommandSender getSender() {
      return this.sender;
   }

   @Override
   public boolean isPlayer() {
      return this.sender instanceof Player;
   }

   @Override
   public boolean isConsole() {
      return this.sender instanceof ConsoleCommandSender;
   }

   @Nullable
   @Override
   public Player getAsPlayer() {
      return this.isPlayer() ? (Player)this.sender : null;
   }

   @NotNull
   @Override
   public Player requirePlayer() {
      if (!this.isPlayer()) {
         throw new SenderNotPlayerException();
      } else {
         return (Player)this.sender;
      }
   }

   @NotNull
   @Override
   public ConsoleCommandSender requireConsole() {
      if (!this.isConsole()) {
         throw new SenderNotConsoleException();
      } else {
         return (ConsoleCommandSender)this.sender;
      }
   }

   @NotNull
   @Override
   public Audience audience() {
      if (this.sender instanceof Audience) {
         return (Audience)this.sender;
      } else {
         BukkitAudiences audiences = (BukkitAudiences)this.handler.bukkitAudiences;
         if (audiences == null) {
            throw new IllegalStateException("You must call BukkitCommandHandler.enableAdventure() to access this method!");
         } else {
            return audiences.sender(this.getSender());
         }
      }
   }

   @Override
   public void reply(@NotNull ComponentLike component) {
      this.audience().sendMessage(component);
   }

   @NotNull
   @Override
   public String getName() {
      return this.sender.getName();
   }

   @NotNull
   @Override
   public UUID getUniqueId() {
      if (this.isPlayer()) {
         return ((Player)this.sender).getUniqueId();
      } else {
         return this.isConsole() ? CONSOLE_UUID : UUID.nameUUIDFromBytes(this.getName().getBytes(StandardCharsets.UTF_8));
      }
   }

   @Override
   public void reply(@NotNull String message) {
      Preconditions.notNull(message, "message");
      this.sender.sendMessage(Strings.colorize(this.handler.getMessagePrefix() + message));
   }

   @Override
   public void error(@NotNull String message) {
      Preconditions.notNull(message, "message");
      this.sender.sendMessage(Strings.colorize(this.handler.getMessagePrefix() + "&c" + message));
   }

   @Override
   public BukkitCommandHandler getCommandHandler() {
      return this.handler;
   }

   @NotNull
   @Override
   public Locale getLocale() {
      if (this.isPlayer()) {
         String playerLocale;
         try {
            playerLocale = this.requirePlayer().getLocale();
         } catch (NoSuchMethodError var4) {
            Optional<MethodCaller> getLocale = BukkitActor.getLocale.get();
            if (!getLocale.isPresent()) {
               return BukkitCommandActor.super.getLocale();
            }

            playerLocale = (String)getLocale.get().call(this.requirePlayer().spigot());
         }

         Locale locale = Locales.get(playerLocale);
         return locale == null ? BukkitCommandActor.super.getLocale() : locale;
      } else {
         return BukkitCommandActor.super.getLocale();
      }
   }
}
