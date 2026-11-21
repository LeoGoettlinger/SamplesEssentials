package net.luckperms.api.event.source;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface Source {
   @NonNull
   Source.Type getType();

   public static enum Type {
      ENTITY,
      UNKNOWN;
   }
}
