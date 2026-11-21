package revxrsal.commands.orphan;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.core.CommandPath;

public final class OrphanRegistry {
   @NotNull
   @Unmodifiable
   private final List<CommandPath> parentPaths;
   @NotNull
   private final OrphanCommand handler;

   @NotNull
   public List<CommandPath> getParentPaths() {
      return this.parentPaths;
   }

   @NotNull
   public OrphanCommand getHandler() {
      return this.handler;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof OrphanRegistry)) {
         return false;
      } else {
         OrphanRegistry other = (OrphanRegistry)o;
         Object this$parentPaths = this.getParentPaths();
         Object other$parentPaths = other.getParentPaths();
         if (this$parentPaths == null ? other$parentPaths == null : this$parentPaths.equals(other$parentPaths)) {
            Object this$handler = this.getHandler();
            Object other$handler = other.getHandler();
            return this$handler == null ? other$handler == null : this$handler.equals(other$handler);
         } else {
            return false;
         }
      }
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $parentPaths = this.getParentPaths();
      result = result * 59 + ($parentPaths == null ? 43 : $parentPaths.hashCode());
      Object $handler = this.getHandler();
      return result * 59 + ($handler == null ? 43 : $handler.hashCode());
   }

   @Override
   public String toString() {
      return "OrphanRegistry(parentPaths=" + this.getParentPaths() + ", handler=" + this.getHandler() + ")";
   }

   OrphanRegistry(@NotNull List<CommandPath> parentPaths, @NotNull OrphanCommand handler) {
      if (parentPaths == null) {
         throw new NullPointerException("parentPaths is marked non-null but is null");
      } else if (handler == null) {
         throw new NullPointerException("handler is marked non-null but is null");
      } else {
         this.parentPaths = parentPaths;
         this.handler = handler;
      }
   }
}
