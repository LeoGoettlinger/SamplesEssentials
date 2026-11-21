package eu.kennytv.maintenance.paper.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import eu.kennytv.maintenance.core.util.SenderInfo;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerConnection;
import io.papermc.paper.connection.PlayerLoginConnection;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class PaperConnectionInfo implements SenderInfo {
   private final PlayerConnection connection;

   public PaperConnectionInfo(PlayerConnection connection) {
      this.connection = connection;
   }

   @Override
   public UUID uuid() {
      return this.profile().getId();
   }

   @Override
   public String name() {
      return this.profile().getName();
   }

   @Override
   public boolean hasPermission(String permission) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void send(Component component) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isPlayer() {
      return this.connection instanceof Player;
   }

   private PlayerProfile profile() {
      if (this.connection instanceof PlayerLoginConnection loginConnection) {
         return loginConnection.getAuthenticatedProfile();
      } else if (this.connection instanceof PlayerConfigurationConnection configurationConnection) {
         return configurationConnection.getProfile();
      } else {
         throw new IllegalArgumentException("Unknown connection type: " + this.connection.getClass());
      }
   }
}
