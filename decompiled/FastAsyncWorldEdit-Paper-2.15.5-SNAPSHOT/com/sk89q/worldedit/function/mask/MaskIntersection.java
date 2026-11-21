package com.sk89q.worldedit.function.mask;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class MaskIntersection extends AbstractMask {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   protected final Set<Mask> masks;
   protected Mask[] masksArray;
   protected boolean defaultReturn;

   public MaskIntersection(Collection<Mask> masks) {
      Preconditions.checkNotNull(masks);
      this.masks = new LinkedHashSet<>(masks);
      this.formArray();
   }

   public static Mask of(Mask... masks) {
      Set<Mask> set = new LinkedHashSet<>();

      for (Mask mask : masks) {
         if (mask == Masks.alwaysFalse()) {
            return mask;
         }

         if (mask != null && mask != Masks.alwaysTrue()) {
            if (mask.getClass() == MaskIntersection.class) {
               set.addAll(((MaskIntersection)mask).getMasks());
            } else {
               set.add(mask);
            }
         }
      }

      switch (set.size()) {
         case 0:
            return Masks.alwaysTrue();
         case 1:
            return set.iterator().next();
         default:
            return new MaskIntersection(set).optimize();
      }
   }

   public MaskIntersection(Mask... mask) {
      this(Arrays.asList((Mask[])Preconditions.checkNotNull(mask)));
   }

   private void formArray() {
      if (this.masks.isEmpty()) {
         this.masksArray = new Mask[]{Masks.alwaysFalse()};
      } else {
         this.masksArray = this.masks.toArray(new Mask[0]);
      }

      this.defaultReturn = this.masksArray.length != 0;
   }

   public Function<Entry<Mask, Mask>, Mask> pairingFunction() {
      return input -> input.getKey().tryCombine(input.getValue());
   }

   private boolean optimizeMasks(Set<Mask> ignore) {
      boolean changed = false;

      for (int i = 0; i < this.masksArray.length; i++) {
         Mask mask = this.masksArray[i];
         if (!ignore.contains(mask)) {
            Mask newMask = mask.tryOptimize();
            if (newMask != null) {
               changed = true;
               this.masksArray[i] = newMask;
            } else {
               ignore.add(mask);
            }
         }
      }

      if (changed) {
         this.masks.clear();
         Collections.addAll(this.masks, this.masksArray);
      }

      boolean formArray = false;

      for (Mask mask : this.masksArray) {
         if (mask.getClass() == this.getClass()) {
            this.masks.remove(mask);
            this.masks.addAll(((MaskIntersection)mask).getMasks());
            formArray = true;
            changed = true;
         }
      }

      if (formArray) {
         this.formArray();
      }

      return changed;
   }

   @Override
   public Mask tryOptimize() {
      int maxIteration = 1000;
      Set<Mask> optimized = new HashSet<>();
      Set<Entry<Mask, Mask>> failedCombines = new HashSet<>();
      boolean changed = false;

      while (this.combineMasks(this.pairingFunction(), failedCombines)) {
         changed = true;
      }

      if (this.masks.isEmpty()) {
         this.formArray();
         return Masks.alwaysTrue();
      } else if (this.masks.size() == 1) {
         this.formArray();
         return this.masks.iterator().next();
      } else {
         do {
            changed |= this.optimizeMasks(optimized);
         } while (this.combineMasks(this.pairingFunction(), failedCombines) && --maxIteration > 0);

         if (maxIteration == 0) {
            LOGGER.error("Failed optimize MaskIntersection");

            for (Mask mask : this.masks) {
               LOGGER.error(mask.getClass() + " / " + mask);
            }
         }

         this.formArray();
         if (this.masks.isEmpty()) {
            return Masks.alwaysTrue();
         } else if (this.masks.size() == 1) {
            return this.masks.iterator().next();
         } else {
            return changed ? this : null;
         }
      }
   }

   private boolean combineMasks(Function<Entry<Mask, Mask>, Mask> pairing, Set<Entry<Mask, Mask>> failedCombines) {
      boolean hasOptimized = false;

      while (true) {
         Mask[] result = null;

         label35:
         for (Mask mask : this.masks) {
            for (Mask other : this.masks) {
               if (mask != other) {
                  SimpleEntry<Mask, Mask> pair = new SimpleEntry<>(mask, other);
                  if (!failedCombines.contains(pair)) {
                     Mask combined = pairing.apply(pair);
                     if (combined != null) {
                        result = new Mask[]{combined, mask, other};
                        break label35;
                     }

                     failedCombines.add(pair);
                  }
               }
            }
         }

         if (result == null) {
            return hasOptimized;
         }

         this.masks.remove(result[1]);
         this.masks.remove(result[2]);
         this.masks.add(result[0]);
         hasOptimized = true;
      }
   }

   public void add(Collection<Mask> masks) {
      Preconditions.checkNotNull(masks);
      this.masks.addAll(masks);
      this.formArray();
   }

   public void add(Mask... mask) {
      this.add(Arrays.asList((Mask[])Preconditions.checkNotNull(mask)));
   }

   public Collection<Mask> getMasks() {
      return this.masks;
   }

   public final Mask[] getMasksArray() {
      return this.masksArray;
   }

   @Override
   public boolean test(BlockVector3 vector) {
      for (Mask mask : this.masksArray) {
         if (!mask.test(vector)) {
            return false;
         }
      }

      return this.defaultReturn;
   }

   @Nullable
   @Override
   public Mask2D toMask2D() {
      List<Mask2D> mask2dList = new ArrayList<>();

      for (Mask mask : this.masks) {
         Mask2D mask2d = mask.toMask2D();
         if (mask2d == null) {
            return null;
         }

         mask2dList.add(mask2d);
      }

      return new MaskIntersection2D(mask2dList);
   }

   @Override
   public Mask copy() {
      Set<Mask> masks = this.masks.stream().map(Mask::copy).collect(Collectors.toSet());
      return new MaskIntersection(masks);
   }

   @Override
   public boolean replacesAir() {
      for (Mask mask : this.masksArray) {
         if (mask.replacesAir()) {
            return true;
         }
      }

      return false;
   }
}
