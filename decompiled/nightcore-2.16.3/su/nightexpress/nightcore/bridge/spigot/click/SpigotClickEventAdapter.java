package su.nightexpress.nightcore.bridge.spigot.click;

import java.util.Objects;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEventCustom;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.dialog.Dialog;
import net.md_5.bungee.api.dialog.chat.ShowDialogClickEvent;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogAdapter;
import su.nightexpress.nightcore.bridge.text.event.NightClickEvent;
import su.nightexpress.nightcore.bridge.text.event.WrappedPayload;

public class SpigotClickEventAdapter {
   public static ClickEvent adaptClickEvent(NightClickEvent event, DialogAdapter<?> dDialogAdapter) {
      WrappedPayload payload = event.payload();
      Objects.requireNonNull(payload);
      Object var3 = payload;

      while (true) {
         ClickEvent var18;
         switch (var3) {
            case WrappedPayload.Custom custom:
               var18 = fromCustom(custom);
               break;
            case WrappedPayload.Dialog dialog:
               var18 = fromDialog(dialog, dDialogAdapter);
               break;
            case WrappedPayload.Int(int var20):
               int var14 = var20;
               if (false) {
                  byte var4 = 3;
                  continue;
               }

               var18 = new ClickEvent(Action.CHANGE_PAGE, String.valueOf(var14));
               break;
            case WrappedPayload.Text(String var16):
               String action = var16;

               Action action = switch (event.action()) {
                  case OPEN_URL -> Action.OPEN_URL;
                  case OPEN_FILE -> Action.OPEN_FILE;
                  case RUN_COMMAND -> Action.RUN_COMMAND;
                  case SUGGEST_COMMAND -> Action.SUGGEST_COMMAND;
                  case COPY_TO_CLIPBOARD -> Action.COPY_TO_CLIPBOARD;
                  default -> throw new IllegalStateException("Unexpected value: " + event.action());
               };
               var18 = new ClickEvent(action, action);
               break;
            default:
               throw new IllegalStateException("Unexpected value: " + payload);
         }

         return var18;
      }
   }

   @NonNull
   public static ClickEvent fromCustom(@NonNull WrappedPayload.Custom custom) {
      return new ClickEventCustom(custom.key().value(), custom.nbt().asString());
   }

   @NonNull
   public static ClickEvent fromDialog(@NonNull WrappedPayload.Dialog dialog, @NonNull DialogAdapter<?> dDialogAdapter) {
      return new ShowDialogClickEvent((Dialog)dDialogAdapter.adaptDialog(dialog.dialog()));
   }
}
