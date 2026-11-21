package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import java.io.File;
import lombok.Generated;

public class LuaPowerConfigFields extends PowersConfigFields {
   private final ScriptDefinition luaPowerDefinition;

   public LuaPowerConfigFields(String fileName, File file, ScriptDefinition luaPowerDefinition) {
      this(fileName, file, luaPowerDefinition, null, PowersConfigFields.PowerType.MISCELLANEOUS);
   }

   public LuaPowerConfigFields(String fileName, File file, ScriptDefinition luaPowerDefinition, String effect, PowersConfigFields.PowerType powerType) {
      super(fileName, true);
      this.filename = fileName;
      this.luaPowerDefinition = luaPowerDefinition;
      this.setEffect(effect);
      this.setPowerType(powerType);
      this.setFile(file);
   }

   @Generated
   public ScriptDefinition getLuaPowerDefinition() {
      return this.luaPowerDefinition;
   }
}
