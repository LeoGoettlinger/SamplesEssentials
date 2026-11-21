package com.artillexstudios.axtrade.libs.lamp.bukkit.adventure;

import com.artillexstudios.axtrade.libs.lamp.bukkit.BukkitCommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.ExecutableCommand;
import com.artillexstudios.axtrade.libs.lamp.process.ResponseHandler;
import java.util.function.Function;
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
