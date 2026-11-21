package dev.vatten.baserad.events;

import dev.vatten.baserad.VattenPlayer;

public class PlayerJoinEvent extends PlayerEvent {
   public PlayerJoinEvent(VattenPlayer player) {
      super(player);
   }
}
