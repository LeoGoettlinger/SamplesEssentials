package su.nightexpress.nightcore.bridge.chat;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

public interface UniversalChatRenderer {
   @NonNull
   NightComponent render(@NonNull Player var1, @NonNull String var2, @NonNull String var3, @Nullable CommandSender var4);
}
