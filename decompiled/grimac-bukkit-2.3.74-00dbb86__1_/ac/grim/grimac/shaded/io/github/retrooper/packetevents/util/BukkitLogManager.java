package ac.grim.grimac.shaded.io.github.retrooper.packetevents.util;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEventsAPI;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.LogManager;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.kyori.adventure.audience.Audience;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.ComponentLike;
import ac.grim.grimac.shaded.kyori.adventure.text.format.NamedTextColor;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ApiStatus.Internal
public final class BukkitLogManager extends LogManager {
   private static final Component PREFIX = Component.text("[packetevents] ").color(NamedTextColor.AQUA);

   public BukkitLogManager(PacketEventsAPI<?> packetevents) {
      super(packetevents);
   }

   @Override
   public void log(Level level, ComponentLike component, @Nullable Throwable error) {
      ComponentLike line = Component.text().append(PREFIX).append(component);
      ConsoleCommandSender sender = Bukkit.getConsoleSender();
      if (sender instanceof Audience) {
         sender.sendMessage(line);
      } else {
         sender.sendMessage(LegacyComponentSerializer.legacySection().serialize(line.asComponent()));
      }

      if (error != null) {
         Bukkit.getLogger().log(level, "", error);
      }
   }
}
