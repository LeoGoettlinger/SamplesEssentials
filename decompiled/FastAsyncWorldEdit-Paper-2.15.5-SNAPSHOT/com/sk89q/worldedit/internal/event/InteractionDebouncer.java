package com.sk89q.worldedit.internal.event;

import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.util.Identifiable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InteractionDebouncer {
   private final Platform platform;
   private final Map<UUID, InteractionDebouncer.Interaction> lastInteractions = new HashMap<>();

   public InteractionDebouncer(Platform platform) {
      this.platform = platform;
   }

   public void clearInteraction(Identifiable player) {
      this.lastInteractions.remove(player.getUniqueId());
   }

   public void setLastInteraction(Identifiable player, boolean result) {
      this.lastInteractions.put(player.getUniqueId(), new InteractionDebouncer.Interaction(this.platform.getTickCount(), result));
   }

   public Optional<Boolean> getDuplicateInteractionResult(Identifiable player) {
      InteractionDebouncer.Interaction last = this.lastInteractions.get(player.getUniqueId());
      if (last == null) {
         return Optional.empty();
      } else {
         long now = this.platform.getTickCount();
         return now - last.tick <= 1L ? Optional.of(last.result) : Optional.empty();
      }
   }

   private record Interaction(long tick, boolean result) {
   }
}
