package su.nightexpress.nightcore.util.text.night.tag;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.text.night.entry.EntryGroup;

public interface TagHandler {
   void handleOpen(@NonNull EntryGroup var1, @Nullable String var2);

   void handleClose(@NonNull EntryGroup var1);

   boolean canBeClosed();
}
