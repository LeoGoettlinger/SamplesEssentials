package ch.njol.skript.lang;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface ExecutionIntent extends Comparable<ExecutionIntent> permits ExecutionIntent.StopTrigger, ExecutionIntent.StopSections {
   @Contract(
      value = " -> new",
      pure = true
   )
   static ExecutionIntent.StopTrigger stopTrigger() {
      return new ExecutionIntent.StopTrigger();
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static ExecutionIntent.StopSections stopSections(int levels) {
      Preconditions.checkArgument(levels > 0, "Depth must be at least 1");
      return new ExecutionIntent.StopSections(levels);
   }

   @Contract(
      value = " -> new",
      pure = true
   )
   static ExecutionIntent.StopSections stopSection() {
      return new ExecutionIntent.StopSections(1);
   }

   @Nullable
   ExecutionIntent use();

   public static final class StopSections implements ExecutionIntent {
      private final int levels;

      private StopSections(int levels) {
         this.levels = levels;
      }

      @Nullable
      public ExecutionIntent.StopSections use() {
         return this.levels > 1 ? new ExecutionIntent.StopSections(this.levels - 1) : null;
      }

      public int compareTo(@NotNull ExecutionIntent other) {
         if (!(other instanceof ExecutionIntent.StopSections)) {
            return other.compareTo(this) * -1;
         } else {
            int levels = ((ExecutionIntent.StopSections)other).levels;
            return Integer.compare(this.levels, levels);
         }
      }

      public int levels() {
         return this.levels;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else {
            return obj != null && obj.getClass() == this.getClass() ? this.levels == ((ExecutionIntent.StopSections)obj).levels : false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.levels);
      }

      @Override
      public String toString() {
         return MoreObjects.toStringHelper(this).add("levels", this.levels).toString();
      }
   }

   public static final class StopTrigger implements ExecutionIntent {
      private StopTrigger() {
      }

      public ExecutionIntent.StopTrigger use() {
         return new ExecutionIntent.StopTrigger();
      }

      public int compareTo(@NotNull ExecutionIntent other) {
         return other instanceof ExecutionIntent.StopTrigger ? 0 : 1;
      }

      @Override
      public String toString() {
         return "StopTrigger";
      }
   }
}
