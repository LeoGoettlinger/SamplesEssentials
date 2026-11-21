package su.nightexpress.nightcore.util.text.night.tag.handler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.text.night.entry.EntryGroup;
import su.nightexpress.nightcore.util.text.night.tag.TagHandler;

public abstract class ClassicTagHandler implements TagHandler {
   protected ClassicTagHandler() {
   }

   @Override
   public void handleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
      this.onHandleOpen(group, tagContent);
   }

   @Override
   public void handleClose(@NonNull EntryGroup group) {
      this.onHandleClose(group);
   }

   protected abstract void onHandleOpen(@NonNull EntryGroup var1, @Nullable String var2);

   protected abstract void onHandleClose(@NonNull EntryGroup var1);

   @Override
   public boolean canBeClosed() {
      return true;
   }
}
