package net.luckperms.api.platform;

import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface PlatformEntity {
   @Nullable
   UUID getUniqueId();

   @NonNull
   String getName();

   @NonNull
   PlatformEntity.Type getType();

   public static enum Type {
      PLAYER,
      CONSOLE;
   }
}
