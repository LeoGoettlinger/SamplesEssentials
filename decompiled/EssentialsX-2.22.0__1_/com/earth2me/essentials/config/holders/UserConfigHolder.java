package com.earth2me.essentials.config.holders;

import com.earth2me.essentials.config.annotations.DeleteIfIncomplete;
import com.earth2me.essentials.config.annotations.DeleteOnEmpty;
import com.earth2me.essentials.config.entities.CommandCooldown;
import com.earth2me.essentials.config.entities.LazyLocation;
import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.MonotonicNonNull;
import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.NonNull;
import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.objectmapping.ConfigSerializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.essentialsx.api.v2.services.mail.MailMessage;
import org.bukkit.Location;
import org.bukkit.Material;

@ConfigSerializable
public class UserConfigHolder {
   @MonotonicNonNull
   private BigDecimal money;
   @DeleteOnEmpty
   @MonotonicNonNull
   private Map<String, LazyLocation> homes;
   @Nullable
   private String nickname;
   @DeleteOnEmpty
   @MonotonicNonNull
   private Set<Material> unlimited;
   @DeleteOnEmpty
   @MonotonicNonNull
   private Map<String, List<String>> powertools;
   @MonotonicNonNull
   private LazyLocation lastlocation;
   @MonotonicNonNull
   private LazyLocation logoutlocation;
   @Nullable
   private String jail;
   @DeleteOnEmpty
   @MonotonicNonNull
   private ArrayList<MailMessage> mail;
   private boolean teleportenabled = true;
   private boolean teleportauto = false;
   @DeleteOnEmpty
   @MonotonicNonNull
   private List<UUID> ignore;
   private boolean godmode = false;
   private boolean flymode = false;
   private boolean muted = false;
   @Nullable
   private String muteReason;
   private boolean jailed = false;
   @NonNull
   private String ipAddress = "";
   private boolean afk = false;
   @DeleteOnEmpty
   @Nullable
   private String geolocation;
   private boolean socialspy = false;
   private boolean npc = false;
   @MonotonicNonNull
   private String lastAccountName;
   @MonotonicNonNull
   private String npcName;
   private boolean powertoolsenabled = true;
   private boolean acceptingPay = true;
   @Nullable
   private Boolean confirmPay;
   @Nullable
   private Boolean confirmClear;
   @Nullable
   private Boolean lastMessageReplyRecipient;
   private boolean baltopExempt = false;
   @MonotonicNonNull
   private Boolean shouting;
   @DeleteOnEmpty
   @MonotonicNonNull
   private List<String> pastUsernames;
   @NonNull
   private UserConfigHolder.Timestamps timestamps = new UserConfigHolder.Timestamps();

   public BigDecimal money() {
      return this.money;
   }

   public void money(final BigDecimal value) {
      this.money = value;
   }

   public Map<String, LazyLocation> homes() {
      if (this.homes == null) {
         this.homes = new HashMap<>();
      }

      return this.homes;
   }

   public void homes(final Map<String, LazyLocation> value) {
      this.homes = value;
   }

   public String nickname() {
      return this.nickname;
   }

   public void nickname(final String value) {
      this.nickname = value;
   }

   public Set<Material> unlimited() {
      if (this.unlimited == null) {
         this.unlimited = new HashSet<>();
      }

      return this.unlimited;
   }

   public Map<String, List<String>> powertools() {
      if (this.powertools == null) {
         this.powertools = new HashMap<>();
      }

      return this.powertools;
   }

   public LazyLocation lastLocation() {
      return this.lastlocation;
   }

   public void lastLocation(final Location value) {
      if (value != null && value.getWorld() != null) {
         this.lastlocation = LazyLocation.fromLocation(value);
      }
   }

   public LazyLocation logoutLocation() {
      return this.logoutlocation;
   }

   public void logoutLocation(final Location value) {
      if (value != null && value.getWorld() != null) {
         this.logoutlocation = LazyLocation.fromLocation(value);
      }
   }

   public String jail() {
      return this.jail;
   }

   public void jail(final String value) {
      this.jail = value;
   }

   public ArrayList<MailMessage> mail() {
      if (this.mail == null) {
         this.mail = new ArrayList<>();
      }

      return this.mail;
   }

   public void mail(final ArrayList<MailMessage> value) {
      this.mail = value;
   }

   public boolean teleportEnabled() {
      return this.teleportenabled;
   }

   public void teleportEnabled(final boolean value) {
      this.teleportenabled = value;
   }

   public boolean teleportAuto() {
      return this.teleportauto;
   }

   public void teleportAuto(final boolean value) {
      this.teleportauto = value;
   }

   public List<UUID> ignore() {
      if (this.ignore == null) {
         this.ignore = new ArrayList<>();
      }

      return this.ignore;
   }

   public void ignore(final List<UUID> value) {
      this.ignore = value;
   }

   public boolean godMode() {
      return this.godmode;
   }

   public void godMode(final boolean value) {
      this.godmode = value;
   }

   public boolean flyMode() {
      return this.flymode;
   }

   public void flyMode(final boolean value) {
      this.flymode = value;
   }

   public boolean muted() {
      return this.muted;
   }

   public void muted(final boolean value) {
      this.muted = value;
   }

   public String muteReason() {
      return this.muteReason;
   }

   public void muteReason(final String value) {
      this.muteReason = value;
   }

   public boolean jailed() {
      return this.jailed;
   }

   public void jailed(final boolean value) {
      this.jailed = value;
   }

   public String ipAddress() {
      return this.ipAddress;
   }

   public void ipAddress(final String value) {
      this.ipAddress = value;
   }

   public boolean afk() {
      return this.afk;
   }

   public void afk(final boolean value) {
      this.afk = value;
   }

   public String geolocation() {
      return this.geolocation;
   }

   public void geolocation(final String value) {
      this.geolocation = value;
   }

   public boolean socialSpy() {
      return this.socialspy;
   }

   public void socialSpy(final boolean value) {
      this.socialspy = value;
   }

   public boolean npc() {
      return this.npc;
   }

   public void npc(final boolean value) {
      this.npc = value;
   }

   public String lastAccountName() {
      return this.lastAccountName;
   }

   public void lastAccountName(final String value) {
      this.lastAccountName = value;
   }

   public String npcName() {
      return this.npcName;
   }

   public void npcName(final String value) {
      this.npcName = value;
   }

   public boolean powerToolsEnabled() {
      return this.powertoolsenabled;
   }

   public void powerToolsEnabled(final boolean value) {
      this.powertoolsenabled = value;
   }

   public boolean acceptingPay() {
      return this.acceptingPay;
   }

   public void acceptingPay(final boolean value) {
      this.acceptingPay = value;
   }

   public Boolean confirmPay() {
      return this.confirmPay;
   }

   public void confirmPay(final Boolean value) {
      this.confirmPay = value;
   }

   public Boolean confirmClear() {
      return this.confirmClear;
   }

   public void confirmClear(final Boolean value) {
      this.confirmClear = value;
   }

   public Boolean lastMessageReplyRecipient() {
      return this.lastMessageReplyRecipient;
   }

   public void lastMessageReplyRecipient(final Boolean value) {
      this.lastMessageReplyRecipient = value;
   }

   public boolean baltopExempt() {
      return this.baltopExempt;
   }

   public void baltopExempt(final boolean value) {
      this.baltopExempt = value;
   }

   public Boolean shouting() {
      return this.shouting;
   }

   public void shouting(final Boolean value) {
      this.shouting = value;
   }

   public List<String> pastUsernames() {
      if (this.pastUsernames == null) {
         this.pastUsernames = new ArrayList<>();
      }

      return this.pastUsernames;
   }

   public void pastUsernames(List<String> value) {
      this.pastUsernames = value;
   }

   public UserConfigHolder.Timestamps timestamps() {
      return this.timestamps;
   }

   @ConfigSerializable
   public static class Timestamps {
      private long lastteleport = 0L;
      private long lastheal = 0L;
      private long mute = 0L;
      private long jail = 0L;
      private long onlinejail = 0L;
      private long logout = 0L;
      private long login = 0L;
      @DeleteOnEmpty
      @MonotonicNonNull
      private Map<String, Long> kits;
      @DeleteOnEmpty
      @DeleteIfIncomplete
      @MonotonicNonNull
      private List<CommandCooldown> commandCooldowns;

      public long lastTeleport() {
         return this.lastteleport;
      }

      public void lastTeleport(final long value) {
         this.lastteleport = value;
      }

      public long lastHeal() {
         return this.lastheal;
      }

      public void lastHeal(final long value) {
         this.lastheal = value;
      }

      public long mute() {
         return this.mute;
      }

      public void mute(final long value) {
         this.mute = value;
      }

      public long jail() {
         return this.jail;
      }

      public void jail(final long value) {
         this.jail = value;
      }

      public long onlineJail() {
         return this.onlinejail;
      }

      public void onlineJail(final long value) {
         this.onlinejail = value;
      }

      public long logout() {
         return this.logout;
      }

      public void logout(final long value) {
         this.logout = value;
      }

      public long login() {
         return this.login;
      }

      public void login(final long value) {
         this.login = value;
      }

      public Map<String, Long> kits() {
         if (this.kits == null) {
            this.kits = new HashMap<>();
         }

         return this.kits;
      }

      public void kits(final Map<String, Long> value) {
         this.kits = value;
      }

      public List<CommandCooldown> commandCooldowns() {
         if (this.commandCooldowns == null) {
            this.commandCooldowns = new ArrayList<>();
         }

         return this.commandCooldowns;
      }

      public void commandCooldowns(final List<CommandCooldown> value) {
         this.commandCooldowns = value;
      }
   }
}
