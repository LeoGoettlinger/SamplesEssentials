package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.math.BlockVector3;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class MaskUnion extends MaskIntersection {
   public MaskUnion(Collection<Mask> masks) {
      super(masks);
   }

   public MaskUnion(Mask... mask) {
      super(mask);
   }

   public static Mask of(Mask... masks) {
      Set<Mask> set = new LinkedHashSet<>();

      for (Mask mask : masks) {
         if (mask == Masks.alwaysTrue()) {
            return mask;
         }

         if (mask != null) {
            if (mask.getClass() == MaskUnion.class) {
               set.addAll(((MaskUnion)mask).getMasks());
            } else {
               set.add(mask);
            }
         }
      }
      return switch (set.size()) {
         case 0 -> Masks.alwaysTrue();
         case 1 -> (Mask)set.iterator().next();
         default -> new MaskUnion(masks).optimize();
      };
   }

   @Override
   public Function<Entry<Mask, Mask>, Mask> pairingFunction() {
      return input -> input.getKey().tryOr(input.getValue());
   }

   @Override
   public boolean test(BlockVector3 vector) {
      Mask[] masks = this.getMasksArray();

      for (Mask mask : masks) {
         if (mask.test(vector)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   @Override
   public Mask2D toMask2D() {
      List<Mask2D> mask2dList = new ArrayList<>();

      for (Mask mask : this.getMasks()) {
         Mask2D mask2d = mask.toMask2D();
         if (mask2d == null) {
            return null;
         }

         mask2dList.add(mask2d);
      }

      return new MaskUnion2D(mask2dList);
   }

   @Override
   public Mask copy() {
      Set<Mask> masksCopy = this.masks.stream().map(Mask::copy).collect(Collectors.toSet());
      return new MaskUnion(masksCopy);
   }

   @Override
   public boolean replacesAir() {
      for (Mask mask : this.getMasksArray()) {
         if (mask.replacesAir()) {
            return true;
         }
      }

      return false;
   }
}
