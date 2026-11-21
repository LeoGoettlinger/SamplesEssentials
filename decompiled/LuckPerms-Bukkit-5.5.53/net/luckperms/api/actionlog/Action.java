package net.luckperms.api.actionlog;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import net.luckperms.api.LuckPermsProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface Action extends Comparable<Action> {
   @NonNull
   static Action.Builder builder() {
      return LuckPermsProvider.get().getActionLogger().actionBuilder();
   }

   @NonNull
   Instant getTimestamp();

   @NonNull
   Action.Source getSource();

   @NonNull
   Action.Target getTarget();

   @NonNull
   String getDescription();

   public interface Builder {
      @NonNull
      Action.Builder timestamp(@NonNull Instant var1);

      @NonNull
      Action.Builder source(@NonNull UUID var1);

      @NonNull
      Action.Builder sourceName(@NonNull String var1);

      @NonNull
      Action.Builder targetType(@NonNull Action.Target.Type var1);

      @NonNull
      Action.Builder target(@Nullable UUID var1);

      @NonNull
      Action.Builder targetName(@NonNull String var1);

      @NonNull
      Action.Builder description(@NonNull String var1);

      @NonNull
      Action build();
   }

   @NonExtendable
   public interface Source {
      @NonNull
      UUID getUniqueId();

      @NonNull
      String getName();
   }

   @NonExtendable
   public interface Target {
      @NonNull
      Optional<UUID> getUniqueId();

      @NonNull
      String getName();

      @NonNull
      Action.Target.Type getType();

      public static enum Type {
         USER,
         GROUP,
         TRACK;
      }
   }
}
