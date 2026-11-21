package dev.vatten.baserad.events;

import dev.vatten.baserad.VattenPlayer;

public class PlayerLeaveEvent extends PlayerEvent {
   public PlayerLeaveEvent(VattenPlayer player) {
      super(player);
   }
}
