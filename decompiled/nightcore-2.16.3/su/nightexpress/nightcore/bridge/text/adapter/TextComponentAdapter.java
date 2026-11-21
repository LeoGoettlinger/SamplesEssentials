package su.nightexpress.nightcore.bridge.text.adapter;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.text.impl.NightKeybindComponent;
import su.nightexpress.nightcore.bridge.text.impl.NightObjectComponent;
import su.nightexpress.nightcore.bridge.text.impl.NightTextComponent;
import su.nightexpress.nightcore.bridge.text.impl.NightTranslatableComponent;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

public interface TextComponentAdapter<C> {
   void send(@NonNull CommandSender var1, @NonNull NightComponent var2);

   void sendActionBar(@NonNull Player var1, @NonNull NightComponent var2);

   @NonNull
   String toJson(@NonNull NightComponent var1);

   @NonNull
   String toLegacy(@NonNull NightComponent var1);

   @NonNull
   C adaptComponent(@NonNull NightComponent var1);

   @NonNull
   C adaptComponent(@NonNull NightTextComponent var1);

   @NonNull
   C adaptComponent(@NonNull NightKeybindComponent var1);

   @NonNull
   C adaptComponent(@NonNull NightTranslatableComponent var1);

   @NonNull
   C adaptComponent(@NonNull NightObjectComponent var1);
}
