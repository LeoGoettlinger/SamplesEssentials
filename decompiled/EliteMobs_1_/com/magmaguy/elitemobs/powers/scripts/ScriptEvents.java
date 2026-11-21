package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.scripts.caching.ScriptEventsBlueprint;
import lombok.Generated;

public class ScriptEvents {
   private final ScriptEventsBlueprint scriptEventsBlueprint;

   public ScriptEvents(ScriptEventsBlueprint eventsBlueprint) {
      this.scriptEventsBlueprint = eventsBlueprint;
   }

   public boolean isTargetEvent(Class clazz) {
      return this.scriptEventsBlueprint.getEvents().contains(clazz);
   }

   public boolean isValid() {
      return true;
   }

   @Generated
   public ScriptEventsBlueprint getScriptEventsBlueprint() {
      return this.scriptEventsBlueprint;
   }
}
