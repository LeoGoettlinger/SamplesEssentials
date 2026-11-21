package su.nightexpress.nightcore.bridge.dialog.adapter;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedConfirmationType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedDialogListType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedDialogType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedMultiActionType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedNoticeType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedServerLinksType;

public interface DialogTypeAdapter<T> {
   @NonNull
   T adaptType(@NonNull WrappedDialogType var1);

   @NonNull
   T adaptType(@NonNull WrappedConfirmationType var1);

   @NonNull
   T adaptType(@NonNull WrappedDialogListType var1);

   @NonNull
   T adaptType(@NonNull WrappedMultiActionType var1);

   @NonNull
   T adaptType(@NonNull WrappedNoticeType var1);

   @NonNull
   T adaptType(@NonNull WrappedServerLinksType var1);
}
