package com.earth2me.essentials;

import com.earth2me.essentials.adventure.ComponentHolder;
import com.earth2me.essentials.api.IAsyncTeleport;
import com.earth2me.essentials.commands.IEssentialsCommand;
import com.earth2me.essentials.config.entities.CommandCooldown;
import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.AfkStatusChangeEvent;
import net.essentialsx.api.v2.services.mail.MailMessage;
import net.essentialsx.api.v2.services.mail.MailSender;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@Deprecated
public interface IUser {
   boolean isAuthorized(String node);

   boolean isAuthorizedCached(String node);

   boolean isAuthorized(IEssentialsCommand cmd);

   boolean isAuthorized(IEssentialsCommand cmd, String permissionPrefix);

   boolean isPermissionSet(String node);

   void healCooldown() throws Exception;

   void giveMoney(BigDecimal value) throws MaxMoneyException;

   void giveMoney(final BigDecimal value, final CommandSource initiator) throws MaxMoneyException;

   void payUser(final User reciever, final BigDecimal value) throws Exception;

   void takeMoney(BigDecimal value);

   void takeMoney(final BigDecimal value, final CommandSource initiator);

   boolean canAfford(BigDecimal value);

   Boolean canSpawnItem(final Material material);

   void setLastLocation();

   void setLogoutLocation();

   void requestTeleport(final User player, final boolean here);

   @Deprecated
   default boolean hasOutstandingTeleportRequest() {
      return this.getNextTpaRequest(false, false, false) != null;
   }

   IAsyncTeleport getAsyncTeleport();

   BigDecimal getMoney();

   void setMoney(final BigDecimal value) throws MaxMoneyException;

   void setAfk(final boolean set, final AfkStatusChangeEvent.Cause cause);

   boolean isHidden();

   boolean isLeavingHidden();

   void setLeavingHidden(boolean leavingHidden);

   void setHidden(boolean vanish);

   boolean isGodModeEnabled();

   String getGroup();

   boolean inGroup(final String group);

   boolean canBuild();

   @Deprecated
   long getTeleportRequestTime();

   void enableInvulnerabilityAfterTeleport();

   void resetInvulnerabilityAfterTeleport();

   boolean hasInvulnerabilityAfterTeleport();

   boolean isVanished();

   void setVanished(boolean vanish);

   boolean isIgnoreExempt();

   void sendMessage(String message);

   void sendComponent(ComponentHolder component);

   ComponentHolder tlComponent(String tlKey, Object... args);

   String playerTl(String tlKey, Object... args);

   void sendTl(String tlKey, Object... args);

   Location getHome(String name) throws Exception;

   Location getHome(Location loc) throws Exception;

   List<String> getHomes();

   void setHome(String name, Location loc);

   void delHome(String name) throws Exception;

   void renameHome(String name, String newName) throws Exception;

   boolean hasHome();

   Location getLastLocation();

   Location getLogoutLocation();

   long getLastTeleportTimestamp();

   void setLastTeleportTimestamp(long time);

   String getJail();

   void setJail(String jail);

   String getFormattedJailTime();

   long getLastActivityTime();

   @Deprecated
   List<String> getMails();

   @Deprecated
   void addMail(String mail);

   void sendMail(MailSender sender, String message);

   void sendMail(MailSender sender, String message, long expireAt);

   ArrayList<MailMessage> getMailMessages();

   void setMailList(ArrayList<MailMessage> messages);

   int getMailAmount();

   boolean isAfk();

   @Deprecated
   void setAfk(final boolean set);

   boolean isIgnoreMsg();

   void setIgnoreMsg(boolean ignoreMsg);

   @Deprecated
   void setConfigProperty(String node, Object object);

   Set<String> getConfigKeys();

   Map<String, Object> getConfigMap();

   Map<String, Object> getConfigMap(String node);

   @Deprecated
   Map<Pattern, Long> getCommandCooldowns();

   List<CommandCooldown> getCooldownsList();

   Date getCommandCooldownExpiry(String label);

   void addCommandCooldown(Pattern pattern, Date expiresAt, boolean save);

   boolean clearCommandCooldown(Pattern pattern);

   Player getBase();

   CommandSource getSource();

   String getName();

   UUID getUUID();

   String getDisplayName();

   String getFormattedNickname();

   String getAfkMessage();

   void setAfkMessage(final String message);

   long getAfkSince();

   boolean isAcceptingPay();

   void setAcceptingPay(boolean acceptingPay);

   boolean isPromptingPayConfirm();

   void setPromptingPayConfirm(boolean prompt);

   boolean isPromptingClearConfirm();

   void setPromptingClearConfirm(boolean prompt);

   boolean isLastMessageReplyRecipient();

   void setLastMessageReplyRecipient(boolean enabled);

   Map<User, BigDecimal> getConfirmingPayments();

   Block getTargetBlock(int maxDistance);

   void setToggleShout(boolean toggleShout);

   boolean isToggleShout();

   @Nullable
   IUser.TpaRequest getNextTpaRequest(boolean inform, boolean ignoreExpirations, boolean excludeHere);

   boolean hasPendingTpaRequests(boolean inform, boolean excludeHere);

   List<String> getPastUsernames();

   void addPastUsername(String username);

   boolean isFreeze();

   void setFreeze(boolean freeze);

   public static class TpaRequest {
      private final String name;
      private final UUID requesterUuid;
      private boolean here;
      private Location location;
      private long time;

      public TpaRequest(String name, UUID requesterUuid) {
         this.name = name;
         this.requesterUuid = requesterUuid;
      }

      public String getName() {
         return this.name;
      }

      public UUID getRequesterUuid() {
         return this.requesterUuid;
      }

      public boolean isHere() {
         return this.here;
      }

      public void setHere(boolean here) {
         this.here = here;
      }

      public Location getLocation() {
         return this.location;
      }

      public void setLocation(Location location) {
         this.location = location;
      }

      public long getTime() {
         return this.time;
      }

      public void setTime(long time) {
         this.time = time;
      }
   }
}
