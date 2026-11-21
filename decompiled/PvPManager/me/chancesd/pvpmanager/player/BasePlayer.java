package me.chancesd.pvpmanager.player;

import java.util.UUID;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.setting.DisplayMode;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.utils.CombatUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class BasePlayer {
   @NotNull
   private final Player player;
   @NotNull
   private final UUID uuid;
   private CombatWorld combatWorld;
   private long actionBarCooldown;
   private String lastActionBarMessage;
   private boolean wasAllowedFlight;
   private final PlayerExemptions exemptions = new PlayerExemptions();

   protected BasePlayer(@NotNull Player player) {
      this.player = player;
      this.uuid = player.getUniqueId();
   }

   @NotNull
   public final Player getPlayer() {
      return this.player;
   }

   @NotNull
   public final String getName() {
      return this.getPlayer().getName();
   }

   @NotNull
   public final UUID getUUID() {
      return this.uuid;
   }

   public final boolean isOnline() {
      return CombatUtils.isOnline(this.uuid);
   }

   public CombatWorld getCombatWorld() {
      return this.combatWorld;
   }

   public void setCombatWorld(CombatWorld combatWorld) {
      this.combatWorld = combatWorld;
   }

   public boolean hasPerm(Permissions permission) {
      return permission.hasPerm(this.getPlayer());
   }

   public final void message(String message) {
      if (this.isOnline() && !message.isEmpty()) {
         this.getPlayer().sendMessage(message);
      }
   }

   public final void messageWithDuration(Lang message, long duration, Object... args) {
      if (this.isOnline()) {
         String formattedMessage = args.length > 0 ? message.msg(args) : message.msg();
         if (!formattedMessage.isEmpty()) {
            if (message.getDisplayMode() == DisplayMode.ACTION_BAR) {
               this.sendActionBar(formattedMessage, duration);
            } else {
               this.getPlayer().sendMessage(formattedMessage);
            }
         }
      }
   }

   public final void message(Lang message, Object... args) {
      this.messageWithDuration(message, 1000L, args);
   }

   public void sendActionBar(String message, long duration) {
      if (System.currentTimeMillis() >= this.actionBarCooldown
         && !message.isEmpty()
         && (System.currentTimeMillis() - this.actionBarCooldown >= 1000L || !message.equals(this.lastActionBarMessage))) {
         if (MCVersion.isAtLeast(MCVersion.V1_10)) {
            if (MCVersion.isAtLeast(MCVersion.V1_20_4)) {
               this.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
            } else if (MCVersion.isAtLeast(MCVersion.V1_16_5)) {
               this.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            } else {
               this.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            }
         }

         this.lastActionBarMessage = message;
         this.actionBarCooldown = System.currentTimeMillis() + duration;
      }
   }

   public void sendActionBar(String message) {
      this.sendActionBar(message, 0L);
   }

   @Override
   public String toString() {
      return "CombatPlayer[" + this.getName() + ", " + this.uuid + "]";
   }

   public boolean getWasAllowedFlight() {
      return this.wasAllowedFlight;
   }

   public void setWasAllowedFlight(boolean wasAllowedFlight) {
      this.wasAllowedFlight = wasAllowedFlight;
   }

   public PlayerExemptions getExemptions() {
      return this.exemptions;
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof BasePlayer) ? false : this.uuid.equals(((BasePlayer)obj).getUUID());
   }

   @Override
   public int hashCode() {
      return this.uuid.hashCode();
   }
}
