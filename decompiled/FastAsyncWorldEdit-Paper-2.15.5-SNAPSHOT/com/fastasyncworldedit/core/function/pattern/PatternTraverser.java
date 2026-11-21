package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.Resettable;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.Pattern;
import java.lang.reflect.Field;
import java.util.Collection;

public class PatternTraverser {
   private final Object pattern;

   public PatternTraverser(Object start) {
      this.pattern = start;
   }

   public void reset(Extent newExtent) {
      this.reset(this.pattern, newExtent);
   }

   private void reset(Object pattern, Extent newExtent) {
      if (pattern != null) {
         if (pattern instanceof Resettable) {
            ((Resettable)pattern).reset();
         }

         for (Class<?> current = pattern.getClass(); current.getSuperclass() != null; current = current.getSuperclass()) {
            if (newExtent != null) {
               try {
                  Field field = current.getDeclaredField("extent");
                  field.setAccessible(true);
                  field.set(pattern, newExtent);
               } catch (IllegalAccessException | ClassCastException | NoSuchFieldException var8) {
               }
            }

            try {
               Field field = current.getDeclaredField("pattern");
               field.setAccessible(true);
               Object next = field.get(pattern);
               this.reset(next, newExtent);
            } catch (IllegalAccessException | ClassCastException | NoSuchFieldException var11) {
            }

            try {
               Field field = current.getDeclaredField("mask");
               field.setAccessible(true);
               Object next = field.get(pattern);
               this.reset(next, newExtent);
            } catch (IllegalAccessException | NoSuchFieldException var10) {
            }

            try {
               Field field = current.getDeclaredField("material");
               field.setAccessible(true);
               Pattern next = (Pattern)field.get(pattern);
               this.reset(next, newExtent);
            } catch (IllegalAccessException | ClassCastException | NoSuchFieldException var9) {
            }

            try {
               Field field = current.getDeclaredField("patterns");
               field.setAccessible(true);

               for (Pattern next : (Collection)field.get(pattern)) {
                  this.reset(next, newExtent);
               }
            } catch (IllegalAccessException | ClassCastException | NoSuchFieldException var12) {
            }
         }
      }
   }
}
