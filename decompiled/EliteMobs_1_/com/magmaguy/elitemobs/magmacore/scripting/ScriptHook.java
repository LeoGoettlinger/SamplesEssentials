package com.magmaguy.elitemobs.magmacore.scripting;

public final class ScriptHook {
   private final String key;
   public static final ScriptHook ON_SPAWN = new ScriptHook("on_spawn");
   public static final ScriptHook ON_TICK = new ScriptHook("on_game_tick");
   public static final ScriptHook ON_ZONE_ENTER = new ScriptHook("on_zone_enter");
   public static final ScriptHook ON_ZONE_LEAVE = new ScriptHook("on_zone_leave");

   public ScriptHook(String key) {
      this.key = key;
   }

   public String getKey() {
      return this.key;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof ScriptHook that ? this.key.equals(that.key) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.key.hashCode();
   }

   @Override
   public String toString() {
      return "ScriptHook{" + this.key + "}";
   }
}
