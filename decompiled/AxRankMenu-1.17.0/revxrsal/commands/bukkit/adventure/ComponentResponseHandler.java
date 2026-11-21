package revxrsal.commands.bukkit.adventure;

import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ResponseHandler;

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
