package net.kyori.adventure.resource;

import java.util.UUID;
import java.util.function.BiConsumer;
import net.kyori.adventure.audience.Audience;

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
