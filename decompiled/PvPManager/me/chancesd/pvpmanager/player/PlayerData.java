package me.chancesd.pvpmanager.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;

public class PlayerData {
   private final boolean defaultData;
   private final UUID uuid;
   private final String name;
   private final String displayName;
   private final boolean pvpEnabled;
   private final long toggleTime;
   private final boolean newbie;
   private final long newbieTimeLeft;
   private final long lastSeen;

   private PlayerData(PlayerData.Builder builder, boolean defaultData) {
      this.defaultData = defaultData;
      this.uuid = builder.uuid;
      this.name = builder.name;
      this.displayName = builder.displayName;
      this.pvpEnabled = builder.pvpEnabled;
      this.toggleTime = builder.toggleTime;
      this.newbie = builder.newbie;
      this.newbieTimeLeft = builder.newbieTimeLeft;
      this.lastSeen = builder.lastSeen;
   }

   public static PlayerData.Builder builder() {
      return new PlayerData.Builder();
   }

   public static PlayerData fromMap(Map<String, Object> userData) {
      if (userData != null && !userData.isEmpty()) {
         UUID uuid = null;
         if (userData.get("uuid") instanceof String string) {
            try {
               uuid = UUID.fromString(string);
            } catch (IllegalArgumentException var19) {
            }
         }

         String name = (String)userData.get("name");
         String displayName = (String)userData.get("displayname");
         boolean pvpEnabled = false;
         Object pvpState = userData.get("pvpstatus");
         if (pvpState instanceof Integer) {
            pvpEnabled = (Integer)pvpState != 0;
         } else if (pvpState instanceof Boolean) {
            pvpEnabled = (Boolean)pvpState;
         }

         long toggleTime = 0L;
         if (userData.get("toggletime") instanceof Number number) {
            toggleTime = number.longValue();
         }

         boolean newbie = false;
         Object newbieState = userData.get("newbie");
         if (newbieState instanceof Integer) {
            newbie = (Integer)newbieState != 0;
         } else if (newbieState instanceof Boolean) {
            newbie = (Boolean)newbieState;
         }

         long newbieTimeLeft = 0L;
         if (userData.get("newbie_timeleft") instanceof Number number) {
            newbieTimeLeft = number.longValue();
         }

         long lastSeen = System.currentTimeMillis();
         if (userData.get("last_seen") instanceof Number number) {
            lastSeen = number.longValue();
         }

         return builder()
            .uuid(uuid)
            .name(name)
            .displayName(displayName)
            .pvpEnabled(pvpEnabled)
            .toggleTime(toggleTime)
            .newbie(newbie)
            .newbieTimeLeft(newbieTimeLeft)
            .lastSeen(lastSeen)
            .build(false);
      } else {
         return createDefault();
      }
   }

   public static PlayerData createDefault() {
      return builder().pvpEnabled(Conf.DEFAULT_PVP.asBool()).build(true);
   }

   public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      if (this.uuid != null) {
         map.put("uuid", this.uuid.toString());
      }

      if (this.name != null) {
         map.put("name", this.name);
      }

      if (this.displayName != null) {
         map.put("displayname", CombatUtils.truncateString(this.displayName, 255));
      }

      map.put("pvpstatus", this.pvpEnabled);
      map.put("toggletime", this.toggleTime);
      map.put("newbie", this.newbie);
      map.put("newbie_timeleft", this.newbieTimeLeft);
      map.put("last_seen", this.lastSeen);
      return map;
   }

   public boolean isDefault() {
      return this.defaultData;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public String getName() {
      return this.name;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public boolean isPvpEnabled() {
      return this.pvpEnabled;
   }

   public long getToggleTime() {
      return this.toggleTime;
   }

   public boolean isNewbie() {
      return this.newbie;
   }

   public long getNewbieTimeLeft() {
      return this.newbieTimeLeft;
   }

   public long getLastSeen() {
      return this.lastSeen;
   }

   public static class Builder {
      private UUID uuid;
      private String name;
      private String displayName;
      private boolean pvpEnabled = false;
      private long toggleTime = 0L;
      private boolean newbie = false;
      private long newbieTimeLeft = 0L;
      private long lastSeen = System.currentTimeMillis();

      public PlayerData.Builder uuid(UUID uuid) {
         this.uuid = uuid;
         return this;
      }

      public PlayerData.Builder name(String name) {
         this.name = name;
         return this;
      }

      public PlayerData.Builder displayName(String displayName) {
         this.displayName = displayName;
         return this;
      }

      public PlayerData.Builder pvpEnabled(boolean pvpEnabled) {
         this.pvpEnabled = pvpEnabled;
         return this;
      }

      public PlayerData.Builder toggleTime(long toggleTime) {
         this.toggleTime = toggleTime;
         return this;
      }

      public PlayerData.Builder newbie(boolean newbie) {
         this.newbie = newbie;
         return this;
      }

      public PlayerData.Builder newbieTimeLeft(long newbieTimeLeft) {
         this.newbieTimeLeft = newbieTimeLeft;
         return this;
      }

      public PlayerData.Builder lastSeen(long lastSeen) {
         this.lastSeen = lastSeen;
         return this;
      }

      public PlayerData build(boolean defaultData) {
         return new PlayerData(this, defaultData);
      }
   }
}
