package eu.kennytv.maintenance.core.util;

import java.util.UUID;
import net.kyori.adventure.text.Component;

public record DummySenderInfo(UUID uuid, String name) implements SenderInfo {
   @Override
   public boolean hasPermission(String permission) {
      return false;
   }

   @Override
   public void send(Component component) {
   }

   @Override
   public boolean isPlayer() {
      return false;
   }
}
