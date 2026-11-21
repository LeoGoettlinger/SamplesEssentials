package com.artillexstudios.axtrade.libs.lamp.bukkit

import com.artillexstudios.axtrade.libs.lamp.bukkit.brigadier.ArgumentTypeResolver
import com.artillexstudios.axtrade.libs.lamp.command.CommandActor
import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter
import com.mojang.brigadier.arguments.ArgumentType
import java.util.function.Consumer
import kotlin.jvm.internal.Intrinsics
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

public final val player: Player
public final val playerOrNull: Player?
public final val sender: CommandSender

public inline fun Plugin.bukkitCommandHandler(crossinline block: (BukkitCommandHandler) -> Unit): BukkitCommandHandler {
   val var10000: BukkitCommandHandler = BukkitCommandHandler.create(`$this$bukkitCommandHandler`);
   block.invoke(var10000);
   return var10000;
}

fun CommandActor.getSender(): CommandSender {
   val var10000: CommandSender = (`$this$sender` as BukkitCommandActor).getSender();
   return var10000;
}

fun CommandActor.getPlayer(): Player {
   val var10000: Player = (`$this$player` as BukkitCommandActor).requirePlayer();
   return var10000;
}

fun CommandActor.getPlayerOrNull(): Player? {
   return (`$this$playerOrNull` as BukkitCommandActor).getAsPlayer();
}

public inline fun BukkitCommandHandler.brigadier(crossinline block: (BukkitBrigadier) -> Unit) {
   `$this$brigadier`.getBrigadier().ifPresent(new Consumer(block) {
      {
         this.$block = `$block`;
      }

      public final void accept(@NotNull BukkitBrigadier it) {
         this.$block.invoke(it);
      }
   });
}

@JvmSynthetic
public inline fun <reified T> BukkitBrigadier.bind(resolver: ArgumentTypeResolver) {
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$bind`.bind(Object::class.java, resolver);
}

@JvmSynthetic
public inline fun <reified T> BukkitBrigadier.bind(crossinline resolver: (CommandParameter) -> ArgumentType<*>?) {
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$bind`.bind(Object::class.java, new ArgumentTypeResolver(resolver) {
      {
         this.$resolver = `$resolver`;
      }

      @Nullable
      @Override
      public final ArgumentType<?> getArgumentType(@NotNull CommandParameter it) {
         return this.$resolver.invoke(it) as ArgumentType<?>;
      }
   });
}

@JvmSynthetic
public inline fun <reified T> BukkitBrigadier.bind(resolver: ArgumentType<*>) {
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$bind`.bind(Object::class.java, resolver);
}
