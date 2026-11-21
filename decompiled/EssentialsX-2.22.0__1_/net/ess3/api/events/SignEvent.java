package net.ess3.api.events;

import com.earth2me.essentials.signs.EssentialsSign;
import net.ess3.api.IUser;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class SignEvent extends Event implements Cancellable {
   final EssentialsSign.ISign sign;
   final EssentialsSign essSign;
   final IUser user;
   private boolean cancelled = false;

   public SignEvent(final EssentialsSign.ISign sign, final EssentialsSign essSign, final IUser user) {
      super(!Bukkit.getServer().isPrimaryThread());
      this.sign = sign;
      this.essSign = essSign;
      this.user = user;
   }

   public EssentialsSign.ISign getSign() {
      return this.sign;
   }

   public EssentialsSign getEssentialsSign() {
      return this.essSign;
   }

   public IUser getUser() {
      return this.user;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(final boolean cancelled) {
      this.cancelled = cancelled;
   }
}
