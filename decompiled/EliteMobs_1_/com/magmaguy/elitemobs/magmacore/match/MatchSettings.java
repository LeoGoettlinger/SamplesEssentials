package com.magmaguy.elitemobs.magmacore.match;

import java.util.Objects;
import lombok.Generated;
import org.bukkit.GameMode;
import org.bukkit.Location;

public final class MatchSettings {
   private final int minPlayers;
   private final int maxPlayers;
   private final Location lobby;
   private final Location start;
   private final Location exit;
   private final String permission;
   private final int countdownSeconds;
   private final GameMode gameMode;
   private final boolean spectatable;
   private final String bypassPermission;
   private final String withinTeleportPermission;
   private final long lingerAfterEndTicks;
   private final boolean destroyAfterEnd;
   private final boolean reusable;
   private final MatchSpace space;
   private final DeathPolicy death;
   private final PlayerCustody custody;
   private final MatchMessages messages;
   private final MatchApi api;

   private MatchSettings(MatchSettings.Builder builder) {
      this.minPlayers = builder.minPlayers;
      this.maxPlayers = builder.maxPlayers;
      this.lobby = clone(builder.lobby);
      this.start = clone(builder.start);
      this.exit = clone(builder.exit);
      this.permission = builder.permission;
      this.countdownSeconds = builder.countdownSeconds;
      this.gameMode = builder.gameMode;
      this.spectatable = builder.spectatable;
      this.bypassPermission = builder.bypassPermission;
      this.withinTeleportPermission = builder.withinTeleportPermission;
      this.lingerAfterEndTicks = builder.lingerAfterEndTicks;
      this.destroyAfterEnd = builder.destroyAfterEnd;
      this.reusable = builder.reusable;
      this.space = builder.space;
      this.death = builder.death;
      this.custody = builder.custody;
      this.messages = builder.messages;
      this.api = builder.api;
   }

   public static MatchSettings.Builder builder() {
      return new MatchSettings.Builder();
   }

   public Location getLobby() {
      return clone(this.lobby);
   }

   public Location getStart() {
      return clone(this.start);
   }

   public Location getExit() {
      return clone(this.exit);
   }

   private static Location clone(Location location) {
      return location == null ? null : location.clone();
   }

   @Generated
   public int getMinPlayers() {
      return this.minPlayers;
   }

   @Generated
   public int getMaxPlayers() {
      return this.maxPlayers;
   }

   @Generated
   public String getPermission() {
      return this.permission;
   }

   @Generated
   public int getCountdownSeconds() {
      return this.countdownSeconds;
   }

   @Generated
   public GameMode getGameMode() {
      return this.gameMode;
   }

   @Generated
   public boolean isSpectatable() {
      return this.spectatable;
   }

   @Generated
   public String getBypassPermission() {
      return this.bypassPermission;
   }

   @Generated
   public String getWithinTeleportPermission() {
      return this.withinTeleportPermission;
   }

   @Generated
   public long getLingerAfterEndTicks() {
      return this.lingerAfterEndTicks;
   }

   @Generated
   public boolean isDestroyAfterEnd() {
      return this.destroyAfterEnd;
   }

   @Generated
   public boolean isReusable() {
      return this.reusable;
   }

   @Generated
   public MatchSpace getSpace() {
      return this.space;
   }

   @Generated
   public DeathPolicy getDeath() {
      return this.death;
   }

   @Generated
   public PlayerCustody getCustody() {
      return this.custody;
   }

   @Generated
   public MatchMessages getMessages() {
      return this.messages;
   }

   @Generated
   public MatchApi getApi() {
      return this.api;
   }

   public static final class Builder {
      private int minPlayers = 1;
      private int maxPlayers = 1;
      private Location lobby;
      private Location start;
      private Location exit;
      private String permission;
      private int countdownSeconds = 3;
      private GameMode gameMode;
      private boolean spectatable;
      private String bypassPermission;
      private String withinTeleportPermission;
      private long lingerAfterEndTicks;
      private boolean destroyAfterEnd = true;
      private boolean reusable;
      private MatchSpace space;
      private DeathPolicy death = DeathPolicy.eliminate();
      private PlayerCustody custody = PlayerCustody.none();
      private MatchMessages messages = MatchMessages.defaults();
      private MatchApi api = MatchApi.NONE;

      private Builder() {
      }

      public MatchSettings.Builder players(int min, int max) {
         if (min >= 0 && max >= 1 && min <= max) {
            this.minPlayers = min;
            this.maxPlayers = max;
            return this;
         } else {
            throw new IllegalArgumentException("Invalid player range " + min + ".." + max);
         }
      }

      public MatchSettings.Builder lobby(Location lobby) {
         this.lobby = lobby;
         return this;
      }

      public MatchSettings.Builder start(Location start) {
         this.start = start;
         return this;
      }

      public MatchSettings.Builder exit(Location exit) {
         this.exit = exit;
         return this;
      }

      public MatchSettings.Builder permission(String permission) {
         this.permission = permission;
         return this;
      }

      public MatchSettings.Builder countdownSeconds(int countdownSeconds) {
         if (countdownSeconds < 0) {
            throw new IllegalArgumentException("Negative countdown");
         } else {
            this.countdownSeconds = countdownSeconds;
            return this;
         }
      }

      public MatchSettings.Builder gameMode(GameMode gameMode) {
         this.gameMode = gameMode;
         return this;
      }

      public MatchSettings.Builder spectatable(boolean spectatable) {
         this.spectatable = spectatable;
         return this;
      }

      public MatchSettings.Builder bypassPermission(String bypassPermission) {
         this.bypassPermission = bypassPermission;
         return this;
      }

      public MatchSettings.Builder withinTeleportPermission(String withinTeleportPermission) {
         this.withinTeleportPermission = withinTeleportPermission;
         return this;
      }

      public MatchSettings.Builder lingerAfterEndTicks(long lingerAfterEndTicks) {
         if (lingerAfterEndTicks < 0L) {
            throw new IllegalArgumentException("Negative linger");
         } else {
            this.lingerAfterEndTicks = lingerAfterEndTicks;
            return this;
         }
      }

      public MatchSettings.Builder destroyAfterEnd(boolean destroyAfterEnd) {
         this.destroyAfterEnd = destroyAfterEnd;
         return this;
      }

      public MatchSettings.Builder reusable(boolean reusable) {
         this.reusable = reusable;
         return this;
      }

      public MatchSettings.Builder space(MatchSpace space) {
         this.space = space;
         return this;
      }

      public MatchSettings.Builder death(DeathPolicy death) {
         this.death = Objects.requireNonNull(death, "death");
         return this;
      }

      public MatchSettings.Builder custody(PlayerCustody custody) {
         this.custody = Objects.requireNonNull(custody, "custody");
         return this;
      }

      public MatchSettings.Builder messages(MatchMessages messages) {
         this.messages = Objects.requireNonNull(messages, "messages");
         return this;
      }

      public MatchSettings.Builder api(MatchApi api) {
         this.api = Objects.requireNonNull(api, "api");
         return this;
      }

      public MatchSettings build() {
         Objects.requireNonNull(this.space, "A match needs a space");
         if (this.reusable && this.space instanceof TemporaryWorlds) {
            throw new IllegalArgumentException("Temporary-world matches cannot be reusable");
         } else {
            return new MatchSettings(this);
         }
      }
   }
}
