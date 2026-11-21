package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.command.util.annotation.PatternList;
import java.io.Serializable;
import org.jspecify.annotations.Nullable;

final class AutoAnnotation_Annotations_patternList implements PatternList, Serializable {
   private static final long serialVersionUID = 0L;

   @Override
   public Class<? extends PatternList> annotationType() {
      return PatternList.class;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("@com.sk89q.worldedit.command.util.annotation.PatternList(");
      return sb.append(')').toString();
   }

   @Override
   public boolean equals(@Nullable Object o) {
      return o == this ? true : o instanceof PatternList;
   }

   @Override
   public int hashCode() {
      return 0;
   }
}
