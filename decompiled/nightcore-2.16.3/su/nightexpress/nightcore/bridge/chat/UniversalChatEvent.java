package su.nightexpress.nightcore.bridge.chat;

import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

public interface UniversalChatEvent {
   boolean isCancelled();

   void setCancelled(boolean var1);

   @NonNull
   Player getPlayer();

   @NonNull
   Set<CommandSender> viewers();

   void editViewers(@NonNull Consumer<Set<CommandSender>> var1);

   void renderer(@NonNull UniversalChatRenderer var1);

   @NonNull
   String message();

   void message(@NonNull NightComponent var1);
}
