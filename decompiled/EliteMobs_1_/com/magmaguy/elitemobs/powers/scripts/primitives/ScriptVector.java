package com.magmaguy.elitemobs.powers.scripts.primitives;

import org.bukkit.util.Vector;

public class ScriptVector {
   private Vector value = null;
   private ScriptFloat x = null;
   private ScriptFloat y = null;
   private ScriptFloat z = null;

   public ScriptVector(ScriptFloat x, ScriptFloat y, ScriptFloat z) {
      this.x = x;
      this.y = y;
      this.z = z;
      if (!x.isRandom() && !y.isRandom() && !z.isRandom()) {
         this.value = new Vector(x.getValue(), y.getValue(), z.getValue());
      }
   }

   public boolean isRandom() {
      return this.value == null;
   }

   public Vector getValue() {
      return this.value != null ? this.value : new Vector(this.x.getValue(), this.y.getValue(), this.z.getValue());
   }
}
