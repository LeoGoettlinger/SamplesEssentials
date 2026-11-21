package com.sk89q.worldedit.function;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class CombinedRegionFunction implements RegionFunction {
   private RegionFunction[] functions;

   public CombinedRegionFunction() {
   }

   public CombinedRegionFunction(Collection<RegionFunction> functions) {
      Preconditions.checkNotNull(functions);
      this.functions = functions.toArray(new RegionFunction[0]);
   }

   public CombinedRegionFunction(RegionFunction... function) {
      this.functions = function;
   }

   public static CombinedRegionFunction combine(RegionFunction function, RegionFunction add) {
      if (function instanceof CombinedRegionFunction combined) {
         combined.add(add);
      } else if (add instanceof CombinedRegionFunction) {
         combined = new CombinedRegionFunction(function);
         combined.add(((CombinedRegionFunction)add).functions);
      } else {
         combined = new CombinedRegionFunction(function, add);
      }

      return combined;
   }

   public void add(Collection<RegionFunction> functions) {
      Preconditions.checkNotNull(functions);
      ArrayList<RegionFunction> functionsList = new ArrayList<>(Arrays.asList(this.functions));
      functionsList.addAll(functions);
      this.functions = functionsList.toArray(new RegionFunction[0]);
   }

   public void add(RegionFunction... function) {
      this.add(Arrays.asList((RegionFunction[])Preconditions.checkNotNull(function)));
   }

   @Override
   public boolean apply(BlockVector3 position) throws WorldEditException {
      boolean ret = false;

      for (RegionFunction function : this.functions) {
         ret |= function.apply(position);
      }

      return ret;
   }
}
