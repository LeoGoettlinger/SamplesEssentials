package ac.grim.grimac.shaded.io.github.retrooper.packetevents.util.viaversion;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ViaVersionAccessor {
   int UNKNOWN_PROTOCOL_VERSION = -1;

   int getProtocolVersion(Player player);

   int getProtocolVersion(User user);

   Class<?> getUserConnectionClass();

   Class<?> getBukkitDecodeHandlerClass();

   Class<?> getBukkitEncodeHandlerClass();
}
