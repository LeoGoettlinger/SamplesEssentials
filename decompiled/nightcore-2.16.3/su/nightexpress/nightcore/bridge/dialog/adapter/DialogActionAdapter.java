package su.nightexpress.nightcore.bridge.dialog.adapter;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.dialog.wrap.action.WrappedDialogAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.action.WrappedDialogCommandTemplateAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.action.WrappedDialogCustomAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.action.WrappedDialogStaticAction;

public interface DialogActionAdapter<A> {
   @NonNull
   A adaptAction(@NonNull WrappedDialogAction var1);

   @NonNull
   A adaptAction(@NonNull WrappedDialogStaticAction var1);

   @NonNull
   A adaptAction(@NonNull WrappedDialogCustomAction var1);

   @NonNull
   A adaptAction(@NonNull WrappedDialogCommandTemplateAction var1);
}
