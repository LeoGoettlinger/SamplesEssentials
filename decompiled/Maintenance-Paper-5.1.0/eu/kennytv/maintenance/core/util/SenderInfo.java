package eu.kennytv.maintenance.core.util;

import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.core.MaintenancePlugin;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public interface SenderInfo {
   UUID uuid();

   String name();

   boolean hasPermission(String var1);

   default boolean hasMaintenancePermission(String permission) {
      return permission == null || this.hasPermission("maintenance.admin") || this.hasPermission("maintenance." + permission);
   }

   void send(Component var1);

   default void sendRich(String message) {
      this.send(MiniMessage.miniMessage().deserialize(message));
   }

   default void sendPrefixedRich(String message) {
      MaintenancePlugin maintenance = (MaintenancePlugin)MaintenanceProvider.get();
      this.send(maintenance.prefix().append(MiniMessage.miniMessage().deserialize(message)));
   }

   boolean isPlayer();
}
