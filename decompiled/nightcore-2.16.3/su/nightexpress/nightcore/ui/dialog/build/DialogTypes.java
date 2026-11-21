package su.nightexpress.nightcore.ui.dialog.build;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedConfirmationType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedDialogListType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedMultiActionType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedNoticeType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedServerLinksType;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

public class DialogTypes {
   @NonNull
   public static WrappedConfirmationType confirmation(@NonNull WrappedActionButton yesButton, @NonNull WrappedActionButton noButton) {
      return new WrappedConfirmationType(yesButton, noButton);
   }

   @NonNull
   public static WrappedDialogListType dialogList(@NonNull List<WrappedDialog> dialogs, @Nullable WrappedActionButton exitAction, int columns, int buttonWidth) {
      return dialogList(dialogs).exitAction(exitAction).columns(columns).buttonWidth(buttonWidth).build();
   }

   @NonNull
   public static WrappedDialogListType.Builder dialogList(@NonNull List<WrappedDialog> dialogs) {
      return new WrappedDialogListType.Builder(dialogs);
   }

   @NonNull
   public static WrappedMultiActionType.Builder multiAction(WrappedActionButton... actions) {
      return multiAction(Lists.newList(actions));
   }

   @NonNull
   public static WrappedMultiActionType.Builder multiAction(@NonNull List<WrappedActionButton> actions) {
      return new WrappedMultiActionType.Builder(actions);
   }

   @NonNull
   public static WrappedNoticeType notice() {
      return notice(DialogButtons.action(TagWrappers.LANG.apply("gui.ok")).build());
   }

   @NonNull
   public static WrappedNoticeType notice(@NonNull WrappedActionButton action) {
      return new WrappedNoticeType(action);
   }

   @NonNull
   public WrappedServerLinksType serverLinks(int columns, int buttonWidth) {
      return this.serverLinks(null, columns, buttonWidth);
   }

   @NonNull
   public WrappedServerLinksType serverLinks(@Nullable WrappedActionButton exitAction, int columns, int buttonWidth) {
      return new WrappedServerLinksType(exitAction, columns, buttonWidth);
   }
}
