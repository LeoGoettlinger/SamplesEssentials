package com.fastasyncworldedit.core.util;

import com.fastasyncworldedit.core.function.mask.ResettableMask;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import java.lang.reflect.Field;
import java.util.Collection;

public class MaskTraverser {
   private final Mask mask;

   public MaskTraverser(Mask start) {
      this.mask = start;
   }

   public void reset(Extent newExtent) {
      this.reset(this.mask, newExtent);
   }

   private void reset(Mask mask, Extent newExtent) {
      if (mask != null) {
         if (mask instanceof ResettableMask) {
            ((ResettableMask)mask).reset();
         }

         for (Class<?> current = mask.getClass(); current.getSuperclass() != null; current = current.getSuperclass()) {
            if (mask instanceof AbstractExtentMask mask1) {
               mask1.setExtent(newExtent);
            } else {
               try {
                  Field field = current.getDeclaredField("extent");
                  field.setAccessible(true);
                  field.set(mask, newExtent);
               } catch (IllegalAccessException | NoSuchFieldException var10) {
               }
            }

            if (mask instanceof MaskIntersection mask1) {
               try {
                  Field field = mask1.getClass().getDeclaredField("masks");
                  field.setAccessible(true);

                  for (Mask next : (Collection)field.get(mask)) {
                     this.reset(next, newExtent);
                  }
               } catch (IllegalAccessException | NoSuchFieldException var11) {
               }
            }

            try {
               Field field = current.getDeclaredField("mask");
               field.setAccessible(true);
               Mask next = (Mask)field.get(mask);
               this.reset(next, newExtent);
            } catch (IllegalAccessException | NoSuchFieldException var9) {
            }

            try {
               Field field = current.getDeclaredField("masks");
               field.setAccessible(true);

               for (Mask next : (Collection)field.get(mask)) {
                  this.reset(next, newExtent);
               }
            } catch (IllegalAccessException | NoSuchFieldException var12) {
            }
         }
      }
   }

   public void setNewExtent(Extent newExtent) {
      this.setNewExtent(this.mask, newExtent);
   }

   private void setNewExtent(Mask mask, Extent newExtent) {
      if (mask != null) {
         for (Class<?> current = mask.getClass(); current.getSuperclass() != null; current = current.getSuperclass()) {
            if (mask instanceof AbstractExtentMask mask1) {
               mask1.setExtent(newExtent);
            } else {
               try {
                  Field field = current.getDeclaredField("extent");
                  field.setAccessible(true);
                  field.set(mask, newExtent);
               } catch (IllegalAccessException | NoSuchFieldException var10) {
               }
            }

            if (mask instanceof MaskIntersection mask1) {
               try {
                  Field field = mask1.getClass().getDeclaredField("masks");
                  field.setAccessible(true);

                  for (Mask next : (Collection)field.get(mask)) {
                     this.setNewExtent(next, newExtent);
                  }
               } catch (IllegalAccessException | NoSuchFieldException var11) {
               }
            }

            try {
               Field field = current.getDeclaredField("mask");
               field.setAccessible(true);
               Mask next = (Mask)field.get(mask);
               this.setNewExtent(next, newExtent);
            } catch (IllegalAccessException | NoSuchFieldException var9) {
            }

            try {
               Field field = current.getDeclaredField("masks");
               field.setAccessible(true);

               for (Mask next : (Collection)field.get(mask)) {
                  this.setNewExtent(next, newExtent);
               }
            } catch (IllegalAccessException | NoSuchFieldException var12) {
            }
         }
      }
   }
}
