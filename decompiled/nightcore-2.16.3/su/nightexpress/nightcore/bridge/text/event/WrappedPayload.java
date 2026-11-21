package su.nightexpress.nightcore.bridge.text.event;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.common.NightKey;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;

public interface WrappedPayload {
   @NonNull
   static WrappedPayload.Text string(@NonNull String value) {
      return new WrappedPayload.Text(value);
   }

   @NonNull
   static WrappedPayload.Int integer(int integer) {
      return new WrappedPayload.Int(integer);
   }

   @NonNull
   static WrappedPayload.Dialog dialog(@NonNull WrappedDialog dialog) {
      return new WrappedPayload.Dialog(dialog);
   }

   @NonNull
   static WrappedPayload.Custom custom(@NonNull NightKey key, @NonNull NightNbtHolder nbt) {
      return new WrappedPayload.Custom(key, nbt);
   }

   public record Custom(@NonNull NightKey key, @NonNull NightNbtHolder nbt) implements WrappedPayload {
   }

   public record Dialog(@NonNull WrappedDialog dialog) implements WrappedPayload {
   }

   public record Int(int integer) implements WrappedPayload {
   }

   public record Text(@NonNull String value) implements WrappedPayload {
   }
}
