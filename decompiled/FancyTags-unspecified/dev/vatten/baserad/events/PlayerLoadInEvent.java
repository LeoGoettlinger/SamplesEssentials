package dev.vatten.baserad.events;

import dev.vatten.baserad.VattenPlayer;

public class PlayerLoadInEvent extends PlayerEvent {
   public PlayerLoadInEvent(VattenPlayer player) {
      super(player);
   }
}
