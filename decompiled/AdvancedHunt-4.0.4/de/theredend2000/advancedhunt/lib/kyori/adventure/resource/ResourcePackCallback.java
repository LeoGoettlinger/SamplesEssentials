package de.theredend2000.advancedhunt.lib.kyori.adventure.resource;

import de.theredend2000.advancedhunt.lib.kyori.adventure.audience.Audience;
import java.util.UUID;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ResourcePackCallback {
   static ResourcePackCallback noOp() {
      return ResourcePackCallbacks.NO_OP;
   }

   static ResourcePackCallback onTerminal(final BiConsumer<UUID, Audience> success, final BiConsumer<UUID, Audience> failure) {
      return (uuid, status, audience) -> {
         if (status == ResourcePackStatus.SUCCESSFULLY_LOADED) {
            success.accept(uuid, audience);
         } else if (!status.intermediate()) {
            failure.accept(uuid, audience);
         }
      };
   }

   void packEventReceived(final UUID uuid, final ResourcePackStatus status, final Audience audience);
}
