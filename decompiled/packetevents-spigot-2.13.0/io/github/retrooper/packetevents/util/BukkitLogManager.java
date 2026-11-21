package io.github.retrooper.packetevents.util;

import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.util.LogManager;
import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import java.util.logging.Level;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Internal
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
