package com.sk89q.worldedit.jlibnoise.module.combiner;

import com.sk89q.worldedit.jlibnoise.exception.NoModuleException;
import com.sk89q.worldedit.jlibnoise.module.Module;

public class Multiply extends Module {
   public Multiply() {
      super(2);
   }

   @Override
   public int GetSourceModuleCount() {
      return 2;
   }

   @Override
   public double GetValue(double x, double y, double z) {
      if (this.SourceModule[0] == null) {
         throw new NoModuleException();
      } else if (this.SourceModule[1] == null) {
         throw new NoModuleException();
      } else {
         return this.SourceModule[0].GetValue(x, y, z) * this.SourceModule[1].GetValue(x, y, z);
      }
   }
}
