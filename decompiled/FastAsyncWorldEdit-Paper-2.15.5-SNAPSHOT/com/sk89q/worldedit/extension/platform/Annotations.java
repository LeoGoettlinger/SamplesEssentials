package com.sk89q.worldedit.extension.platform;

import com.google.auto.value.AutoAnnotation;
import com.sk89q.worldedit.command.util.annotation.PatternList;
import com.sk89q.worldedit.internal.annotation.Radii;

class Annotations {
   @AutoAnnotation
   static Radii radii(int value) {
      return new AutoAnnotation_Annotations_radii(value);
   }

   @AutoAnnotation
   static PatternList patternList() {
      return new AutoAnnotation_Annotations_patternList();
   }

   private Annotations() {
   }
}
