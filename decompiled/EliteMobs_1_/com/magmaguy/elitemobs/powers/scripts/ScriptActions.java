package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptActionsBlueprint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

public class ScriptActions {
   private final List<ScriptAction> scriptActionsList = new ArrayList<>();

   public ScriptActions(ScriptActionsBlueprint scriptActionsBlueprint, Map<String, ScriptExecutable> eliteScriptMap, ScriptRuntimeOwner runtimeOwner) {
      scriptActionsBlueprint.getScriptActionsBlueprintList()
         .forEach(scriptActionBlueprint -> this.scriptActionsList.add(new ScriptAction(scriptActionBlueprint, eliteScriptMap, runtimeOwner)));
   }

   public void runScripts(EliteEntity eliteEntity, LivingEntity directTarget, Event event) {
      for (ScriptAction scriptAction : this.scriptActionsList) {
         scriptAction.runScript(eliteEntity, directTarget, event);
      }
   }

   public void runScripts(ScriptActionData previousScriptActiondata) {
      for (ScriptAction scriptAction : this.scriptActionsList) {
         scriptAction.runScript(previousScriptActiondata);
      }
   }

   public void runScripts(ScriptActionData previousActionData, Location landingLocation) {
      for (ScriptAction scriptAction : this.scriptActionsList) {
         scriptAction.runScript(previousActionData, landingLocation);
      }
   }

   public boolean isValid() {
      return true;
   }

   void close(boolean finishDeathActions) {
      this.scriptActionsList.forEach(action -> action.close(finishDeathActions));
   }

   @Generated
   public List<ScriptAction> getScriptActionsList() {
      return this.scriptActionsList;
   }
}
