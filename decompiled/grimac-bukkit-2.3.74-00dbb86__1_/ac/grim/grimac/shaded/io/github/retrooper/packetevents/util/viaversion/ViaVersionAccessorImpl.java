package ac.grim.grimac.shaded.io.github.retrooper.packetevents.util.viaversion;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.User;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.reflection.Reflection;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaChannelHandler;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.bukkit.handlers.BukkitDecodeHandler;
import com.viaversion.viaversion.bukkit.handlers.BukkitEncodeHandler;
import io.netty.channel.Channel;
import java.lang.reflect.Field;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ViaVersionAccessorImpl implements ViaVersionAccessor {
   private static final boolean HAS_INFO_PROTOCOL_VERSION = Reflection.getClassByNameWithoutException(
         "com.viaversion.viaversion.api.protocol.version.VersionType"
      )
      != null;
   private static final boolean HAS_VIA_CHANNEL_HANDLER = Reflection.getClassByNameWithoutException("com.viaversion.viaversion.api.platform.ViaChannelHandler")
      != null;
   @MonotonicNonNull
   private static Field CONNECTION_FIELD = null;

   @Override
   public int getProtocolVersion(Player player) {
      ViaAPI<Player> api = Via.getAPI();
      return api.getPlayerVersion(player);
   }

   @Override
   public int getProtocolVersion(User user) {
      ViaInjector viaInjector = Via.getManager().getInjector();
      Channel channel = (Channel)user.getChannel();
      Object viaEncoder = channel.pipeline().get(viaInjector.getEncoderName());
      UserConnection connection = null;
      if (HAS_VIA_CHANNEL_HANDLER) {
         if (viaEncoder instanceof ViaChannelHandler) {
            connection = ((ViaChannelHandler)viaEncoder).connection();
         }
      } else {
         if (CONNECTION_FIELD == null) {
            CONNECTION_FIELD = Reflection.getField(viaEncoder.getClass(), UserConnection.class, 0);
         }

         if (CONNECTION_FIELD != null) {
            try {
               connection = (UserConnection)CONNECTION_FIELD.get(viaEncoder);
            } catch (IllegalAccessException var7) {
            }
         }
      }

      return connection != null ? this.getVersion(connection) : -1;
   }

   private int getVersion(UserConnection connection) {
      if (HAS_INFO_PROTOCOL_VERSION) {
         ProtocolVersion version = connection.getProtocolInfo().protocolVersion();
         return version.isKnown() ? version.getVersion() : -1;
      } else {
         return connection.getProtocolInfo().getProtocolVersion();
      }
   }

   @Override
   public Class<?> getUserConnectionClass() {
      return UserConnection.class;
   }

   @Override
   public Class<?> getBukkitDecodeHandlerClass() {
      return BukkitDecodeHandler.class;
   }

   @Override
   public Class<?> getBukkitEncodeHandlerClass() {
      return BukkitEncodeHandler.class;
   }
}
