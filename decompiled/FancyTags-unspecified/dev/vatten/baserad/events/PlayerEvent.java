package dev.vatten.baserad.events;

import dev.vatten.baserad.VattenPlayer;
import lombok.Generated;

public abstract class PlayerEvent extends Event {
   private final VattenPlayer player;

   public PlayerEvent(VattenPlayer player) {
      this.player = player;
   }

   @Generated
   public VattenPlayer getPlayer() {
      return this.player;
   }
}
