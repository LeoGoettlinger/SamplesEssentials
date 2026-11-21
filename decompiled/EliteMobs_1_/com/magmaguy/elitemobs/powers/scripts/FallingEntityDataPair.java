package com.magmaguy.elitemobs.powers.scripts;

import lombok.Generated;

public class FallingEntityDataPair {
   private final ScriptAction scriptAction;
   private final ScriptActionData scriptActionData;

   public FallingEntityDataPair(ScriptAction scriptAction, ScriptActionData scriptActionData) {
      this.scriptAction = scriptAction;
      this.scriptActionData = scriptActionData;
   }

   @Generated
   public ScriptAction getScriptAction() {
      return this.scriptAction;
   }

   @Generated
   public ScriptActionData getScriptActionData() {
      return this.scriptActionData;
   }
}
