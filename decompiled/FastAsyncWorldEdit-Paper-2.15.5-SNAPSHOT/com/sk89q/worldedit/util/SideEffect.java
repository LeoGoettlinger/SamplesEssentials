package com.sk89q.worldedit.util;

import com.fastasyncworldedit.core.configuration.Settings;
import java.util.Locale;

public enum SideEffect {
   HISTORY(SideEffect.State.ON, true),
   HEIGHTMAPS(SideEffect.State.ON, true),
   LIGHTING(Settings.settings().LIGHTING.MODE == 0 ? SideEffect.State.OFF : SideEffect.State.ON, true),
   NEIGHBORS(SideEffect.State.OFF, true),
   UPDATE(SideEffect.State.OFF, true),
   ENTITY_EVENTS(SideEffect.State.ON, false),
   VALIDATION(SideEffect.State.OFF, true),
   ENTITY_AI(SideEffect.State.OFF, true),
   EVENTS(SideEffect.State.OFF, true),
   POI_UPDATE(SideEffect.State.OFF, false),
   NETWORK(SideEffect.State.OFF, false);

   private final String displayName = "worldedit.sideeffect." + this.name().toLowerCase(Locale.US);
   private final String description = "worldedit.sideeffect." + this.name().toLowerCase(Locale.US) + ".description";
   private final SideEffect.State defaultValue;
   private final boolean exposed;

   private SideEffect(SideEffect.State defaultValue, boolean exposed) {
      this.defaultValue = defaultValue;
      this.exposed = exposed;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getDescription() {
      return this.description;
   }

   public SideEffect.State getDefaultValue() {
      return this.defaultValue;
   }

   public boolean isExposed() {
      return this.exposed;
   }

   public static enum State {
      OFF,
      ON,
      DELAYED;

      private final String displayName = "worldedit.sideeffect.state." + this.name().toLowerCase(Locale.US);

      public String getDisplayName() {
         return this.displayName;
      }
   }
}
