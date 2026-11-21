package su.nightexpress.nightcore.bridge.text.event;

import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class NightClickEvent {
   private final NightClickEvent.Action action;
   private final WrappedPayload payload;

   public NightClickEvent(@NonNull NightClickEvent.Action action, @NonNull WrappedPayload payload) {
      this.action = action;
      this.payload = payload;
   }

   @NonNull
   public NightClickEvent.Action action() {
      return this.action;
   }

   @NonNull
   public WrappedPayload payload() {
      return this.payload;
   }

   @Override
   public boolean equals(@Nullable Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         NightClickEvent that = (NightClickEvent)other;
         return this.action == that.action && Objects.equals(this.payload, that.payload);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.action.hashCode();
      return 31 * result + this.payload.hashCode();
   }

   public static enum Action {
      OPEN_URL("open_url"),
      OPEN_FILE("open_file"),
      RUN_COMMAND("run_command"),
      SUGGEST_COMMAND("suggest_command"),
      CHANGE_PAGE("change_page"),
      COPY_TO_CLIPBOARD("copy_to_clipboard"),
      SHOW_DIALOG("show_dialog"),
      CUSTOM("custom");

      private final String name;

      private Action(@NonNull String name) {
         this.name = name;
      }

      @Override
      public String toString() {
         return this.name;
      }
   }
}
