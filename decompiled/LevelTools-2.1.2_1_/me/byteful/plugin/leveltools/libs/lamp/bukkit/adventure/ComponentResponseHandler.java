package me.byteful.plugin.leveltools.libs.lamp.bukkit.adventure;

import java.util.function.Function;
import me.byteful.plugin.leveltools.libs.lamp.bukkit.BukkitCommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import me.byteful.plugin.leveltools.libs.lamp.process.ResponseHandler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class ComponentResponseHandler implements ResponseHandler<ComponentLike> {
   private final Function<CommandSender, Audience> audiences;

   public ComponentResponseHandler(Function<CommandSender, Audience> audiences) {
      this.audiences = audiences;
   }

   public void handleResponse(ComponentLike response, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
      BukkitCommandActor bActor = (BukkitCommandActor)actor;
      if (response != null) {
         this.audiences.apply(bActor.getSender()).sendMessage(response);
      }
   }
}
