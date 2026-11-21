package me.chancesd.pvpmanager.player.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatWorld {
   private final UUID uuid;
   private final String name;
   private boolean pvpState;
   private CombatWorld.WorldOptionState forcePVP;

   public CombatWorld(UUID uuid, String name, boolean pvpState, CombatWorld.WorldOptionState forcePVP) {
      this.uuid = uuid;
      this.name = name;
      this.pvpState = pvpState;
      this.forcePVP = forcePVP;
   }

   public boolean isCombatAllowed() {
      return this.pvpState;
   }

   public CombatWorld.WorldOptionState isPvPForced() {
      return this.forcePVP;
   }

   public void setCombatAllowed(boolean state) {
      this.pvpState = state;
   }

   public void setForcePVP(CombatWorld.WorldOptionState state) {
      this.forcePVP = state;
   }

   public String getName() {
      return this.name;
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public final Map<String, Object> getWorldData() {
      Map<String, Object> userData = new HashMap<>();
      userData.put("uuid", this.getUUID().toString());
      userData.put("name", this.getName());
      userData.put("pvp", this.isCombatAllowed());
      userData.put("forcepvp", this.isPvPForced().name());
      return userData;
   }

   public static enum WorldOptionState {
      ON,
      OFF,
      NONE;

      public static CombatWorld.WorldOptionState fromBoolean(boolean state) {
         return state ? ON : OFF;
      }

      public static CombatWorld.WorldOptionState getOpposite(CombatWorld.WorldOptionState state) {
         if (state == ON) {
            return OFF;
         } else {
            return state == OFF ? ON : NONE;
         }
      }
   }
}
