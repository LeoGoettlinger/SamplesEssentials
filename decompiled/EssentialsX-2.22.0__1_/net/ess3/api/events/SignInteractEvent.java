package net.ess3.api.events;

import com.earth2me.essentials.signs.EssentialsSign;
import net.ess3.api.IUser;
import org.bukkit.event.HandlerList;

public class SignInteractEvent extends SignEvent {
   private static final HandlerList handlers = new HandlerList();

   public SignInteractEvent(final EssentialsSign.ISign sign, final EssentialsSign essSign, final IUser user) {
      super(sign, essSign, user);
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
