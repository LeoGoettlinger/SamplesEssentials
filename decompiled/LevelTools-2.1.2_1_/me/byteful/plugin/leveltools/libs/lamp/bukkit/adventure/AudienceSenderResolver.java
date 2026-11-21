package me.byteful.plugin.leveltools.libs.lamp.bukkit.adventure;

import java.util.function.Function;
import me.byteful.plugin.leveltools.libs.lamp.bukkit.BukkitCommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import me.byteful.plugin.leveltools.libs.lamp.process.SenderResolver;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class AudienceSenderResolver implements SenderResolver {
   private final Function<CommandSender, Audience> audiences;

   public AudienceSenderResolver(Function<CommandSender, Audience> audiences) {
      this.audiences = audiences;
   }

   @Override
   public boolean isCustomType(Class<?> type) {
      return Audience.class.isAssignableFrom(type);
   }

   @NotNull
   @Override
   public Object getSender(@NotNull Class<?> customSenderType, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
      BukkitCommandActor bActor = (BukkitCommandActor)actor;
      return this.audiences.apply(bActor.getSender());
   }
}
