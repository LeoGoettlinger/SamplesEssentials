package com.earth2me.essentials;

import com.earth2me.essentials.adventure.ComponentHolder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSource {
   protected Essentials ess;
   protected CommandSender sender;

   public CommandSource(final Essentials ess, final CommandSender base) {
      this.ess = ess;
      this.sender = base;
   }

   public final CommandSender getSender() {
      return this.sender;
   }

   public final Player getPlayer() {
      return this.sender instanceof Player ? (Player)this.sender : null;
   }

   public void sendTl(final String tlKey, final Object... args) {
      if (this.isPlayer()) {
         this.getUser().sendTl(tlKey, args);
      } else {
         String translation = I18n.tlLiteral(tlKey, args);
         if (!translation.isEmpty()) {
            this.sendComponent(this.ess.getAdventureFacet().deserializeMiniMessage(translation));
         }
      }
   }

   public String tl(final String tlKey, final Object... args) {
      return this.isPlayer() ? this.getUser().playerTl(tlKey, args) : I18n.tlLiteral(tlKey, args);
   }

   public ComponentHolder tlComponent(final String tlKey, final Object... args) {
      if (this.isPlayer()) {
         return this.getUser().tlComponent(tlKey, args);
      } else {
         String translation = I18n.tlLiteral(tlKey, args);
         return this.ess.getAdventureFacet().deserializeMiniMessage(translation);
      }
   }

   public void sendComponent(final ComponentHolder component) {
      this.ess.getAdventureFacet().send(this.sender, component);
   }

   public final net.ess3.api.IUser getUser() {
      return this.sender instanceof Player ? this.ess.getUser((Player)this.sender) : null;
   }

   public final boolean isPlayer() {
      return this.sender instanceof Player;
   }

   public final CommandSender setSender(final CommandSender base) {
      return this.sender = base;
   }

   public void sendMessage(final String message) {
      if (!message.isEmpty()) {
         this.sender.sendMessage(message);
      }
   }

   public boolean isAuthorized(final String permission) {
      return !(this.sender instanceof Player) || this.getUser().isAuthorized(permission);
   }

   public String getSelfSelector() {
      return this.sender instanceof Player ? this.getPlayer().getName() : "*";
   }

   public String getDisplayName() {
      return this.sender instanceof Player ? this.getPlayer().getDisplayName() : this.getSender().getName();
   }
}
