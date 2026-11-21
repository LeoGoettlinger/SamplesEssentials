package su.nightexpress.nightcore.bridge.dialog.adapter;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedBooleanDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedNumberRangeDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.single.WrappedSingleOptionDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedTextDialogInput;

public interface DialogInputAdapter<I> {
   @NonNull
   I adaptInput(@NonNull WrappedDialogInput var1);

   @NonNull
   I adaptInput(@NonNull WrappedTextDialogInput var1);

   @NonNull
   I adaptInput(@NonNull WrappedSingleOptionDialogInput var1);

   @NonNull
   I adaptInput(@NonNull WrappedBooleanDialogInput var1);

   @NonNull
   I adaptInput(@NonNull WrappedNumberRangeDialogInput var1);
}
