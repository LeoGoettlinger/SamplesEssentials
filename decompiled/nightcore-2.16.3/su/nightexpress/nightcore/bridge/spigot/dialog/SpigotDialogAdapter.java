package su.nightexpress.nightcore.bridge.spigot.dialog;

import com.google.gson.JsonObject;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.dialog.ConfirmationDialog;
import net.md_5.bungee.api.dialog.Dialog;
import net.md_5.bungee.api.dialog.DialogBase;
import net.md_5.bungee.api.dialog.DialogListDialog;
import net.md_5.bungee.api.dialog.MultiActionDialog;
import net.md_5.bungee.api.dialog.NoticeDialog;
import net.md_5.bungee.api.dialog.ServerLinksDialog;
import net.md_5.bungee.api.dialog.DialogBase.AfterAction;
import net.md_5.bungee.api.dialog.action.Action;
import net.md_5.bungee.api.dialog.action.ActionButton;
import net.md_5.bungee.api.dialog.action.CustomClickAction;
import net.md_5.bungee.api.dialog.action.RunCommandAction;
import net.md_5.bungee.api.dialog.action.StaticAction;
import net.md_5.bungee.api.dialog.body.DialogBody;
import net.md_5.bungee.api.dialog.body.PlainMessageBody;
import net.md_5.bungee.api.dialog.input.BooleanInput;
import net.md_5.bungee.api.dialog.input.DialogInput;
import net.md_5.bungee.api.dialog.input.InputOption;
import net.md_5.bungee.api.dialog.input.NumberRangeInput;
import net.md_5.bungee.api.dialog.input.SingleOptionInput;
import net.md_5.bungee.api.dialog.input.TextInput;
import net.md_5.bungee.api.dialog.input.TextInput.Multiline;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.common.NightKey;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogActionAdapter;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogAdapter;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogBaseAdapter;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogBodyAdapter;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogButtonAdapter;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogInputAdapter;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogTypeAdapter;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.action.WrappedDialogAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.action.WrappedDialogCommandTemplateAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.action.WrappedDialogCustomAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.action.WrappedDialogStaticAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.base.WrappedDialogAfterAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.base.WrappedDialogBase;
import su.nightexpress.nightcore.bridge.dialog.wrap.body.WrappedDialogBody;
import su.nightexpress.nightcore.bridge.dialog.wrap.body.WrappedItemDialogBody;
import su.nightexpress.nightcore.bridge.dialog.wrap.body.WrappedPlainMessageDialogBody;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedBooleanDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedNumberRangeDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.single.WrappedSingleOptionDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.single.WrappedSingleOptionEntry;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedMultilineOptions;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedTextDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedConfirmationType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedDialogListType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedDialogType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedMultiActionType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedNoticeType;
import su.nightexpress.nightcore.bridge.dialog.wrap.type.WrappedServerLinksType;
import su.nightexpress.nightcore.bridge.spigot.SpigotBridge;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.night.NightMessage;

public class SpigotDialogAdapter
   implements DialogAdapter<Dialog>,
   DialogActionAdapter<Action>,
   DialogBaseAdapter<DialogBase>,
   DialogBodyAdapter<DialogBody>,
   DialogButtonAdapter<ActionButton>,
   DialogInputAdapter<DialogInput>,
   DialogTypeAdapter<Dialog> {
   private final SpigotBridge bridge;
   private DialogBase tempBase;

   public SpigotDialogAdapter(@NonNull SpigotBridge bridge) {
      this.bridge = bridge;
   }

   @NonNull
   private BaseComponent adaptComponent(@NonNull String component) {
      return this.adaptComponent(NightMessage.parse(component));
   }

   @NonNull
   private BaseComponent adaptComponent(@NonNull NightComponent component) {
      return this.bridge.getTextComponentAdapter().adaptComponent(component);
   }

   @NonNull
   public Dialog adaptDialog(@NonNull WrappedDialog wrappedDialog) {
      WrappedDialogBase wrappedBase = wrappedDialog.base();
      WrappedDialogType wrappedType = wrappedDialog.type();
      this.tempBase = this.adaptBase(wrappedBase);
      Dialog type = this.adaptType(wrappedType);
      this.tempBase = null;
      return type;
   }

   @NonNull
   public Action adaptAction(@NonNull WrappedDialogAction action) {
      return action.adapt(this);
   }

   @NonNull
   public StaticAction adaptAction(@NonNull WrappedDialogStaticAction action) {
      ClickEvent clickEvent = this.bridge.getTextComponentAdapter().adaptClickEvent(action.clickEvent());
      return new StaticAction(clickEvent);
   }

   @NonNull
   public CustomClickAction adaptAction(@NonNull WrappedDialogCustomAction action) {
      CustomClickAction handle = new CustomClickAction(NightKey.key("nightcore_dialogs", action.id()).asString());
      handle.additions(action.nbt() == null ? null : action.nbt().payload());
      return handle;
   }

   @NonNull
   public RunCommandAction adaptAction(@NonNull WrappedDialogCommandTemplateAction action) {
      return new RunCommandAction(action.template());
   }

   @NonNull
   private AfterAction adaptAfterAction(@NonNull WrappedDialogAfterAction action) {
      return switch (action) {
         case NONE -> AfterAction.NONE;
         case CLOSE -> AfterAction.CLOSE;
         case WAIT_FOR_RESPONSE -> AfterAction.WAIT_FOR_RESPONSE;
      };
   }

   @NonNull
   public DialogBase adaptBase(@NonNull WrappedDialogBase base) {
      BaseComponent title = this.adaptComponent(base.title());
      BaseComponent externalTitle = base.externalTitle() == null ? null : this.adaptComponent(base.externalTitle());
      boolean canCloseWithEscape = base.canCloseWithEscape();
      boolean pause = base.pause();
      AfterAction afterAction = this.adaptAfterAction(base.afterAction());
      List<DialogBody> body = Lists.modify(base.body(), this::adaptBody);
      List<DialogInput> inputs = Lists.modify(base.inputs(), this::adaptInput);
      return new DialogBase(title, externalTitle, inputs, body, canCloseWithEscape, pause, afterAction);
   }

   @NonNull
   public DialogBody adaptBody(@NonNull WrappedDialogBody body) {
      return body.adapt(this);
   }

   @NonNull
   public DialogBody adaptBody(@NonNull WrappedItemDialogBody body) {
      return new PlainMessageBody(this.adaptComponent(body.description() == null ? "" : body.description().contents()), body.width());
   }

   @NonNull
   public PlainMessageBody adaptBody(@NonNull WrappedPlainMessageDialogBody body) {
      BaseComponent contents = this.adaptComponent(body.contents());
      int width = body.width();
      return new PlainMessageBody(contents, width);
   }

   @NonNull
   public ActionButton adaptButton(@NonNull WrappedActionButton wrappedButton) {
      WrappedDialogAction wrappedAction = wrappedButton.action();
      if (wrappedAction == null) {
         wrappedAction = new WrappedDialogCustomAction("empty", NightNbtHolder.fromJson(new JsonObject()));
      }

      BaseComponent label = this.adaptComponent(wrappedButton.label());
      BaseComponent tooltip = wrappedButton.tooltip() == null ? null : this.adaptComponent(wrappedButton.tooltip());
      Action action = this.adaptAction(wrappedAction);
      int width = wrappedButton.width();
      return new ActionButton(label, tooltip, width, action);
   }

   @NonNull
   public DialogInput adaptInput(@NonNull WrappedDialogInput input) {
      return input.adapt(this);
   }

   @NonNull
   public TextInput adaptInput(@NonNull WrappedTextDialogInput input) {
      String key = input.key();
      int width = input.width();
      BaseComponent label = this.adaptComponent(input.label());
      boolean labelVisible = input.labelVisible();
      String initial = input.initial();
      int maxLength = input.maxLength();
      WrappedMultilineOptions wrappedMultiline = input.multiline();
      Multiline multilineOptions = wrappedMultiline == null ? null : new Multiline(wrappedMultiline.maxLines(), wrappedMultiline.height());
      return new TextInput(key, width, label, labelVisible, initial, maxLength, multilineOptions);
   }

   @NonNull
   private InputOption adaptEntry(@NonNull WrappedSingleOptionEntry wrappedEntry) {
      String id = wrappedEntry.id();
      BaseComponent display = this.adaptComponent(wrappedEntry.display());
      boolean initial = wrappedEntry.initial();
      return new InputOption(id, display, initial);
   }

   @NonNull
   public SingleOptionInput adaptInput(@NonNull WrappedSingleOptionDialogInput input) {
      String key = input.key();
      int width = input.width();
      List<InputOption> entries = Lists.modify(input.entries(), this::adaptEntry);
      BaseComponent label = this.adaptComponent(input.label());
      boolean labelVisible = input.labelVisible();
      return new SingleOptionInput(key, width, label, labelVisible, entries);
   }

   @NonNull
   public BooleanInput adaptInput(@NonNull WrappedBooleanDialogInput input) {
      String key = input.key();
      BaseComponent label = this.adaptComponent(input.label());
      boolean initial = input.initial();
      String onTrue = input.onTrue();
      String onFalse = input.onFalse();
      return new BooleanInput(key, label, initial, onTrue, onFalse);
   }

   @NonNull
   public NumberRangeInput adaptInput(@NonNull WrappedNumberRangeDialogInput input) {
      String key = input.key();
      int width = input.width();
      BaseComponent label = this.adaptComponent(input.label());
      String labelFormat = input.labelFormat();
      float start = input.start();
      float end = input.end();
      Float initial = input.initial();
      Float step = input.step();
      return new NumberRangeInput(key, width, label, labelFormat, start, end, initial, step);
   }

   @NonNull
   public Dialog adaptType(@NonNull WrappedDialogType type) {
      return type.adapt(this);
   }

   @NonNull
   public ConfirmationDialog adaptType(@NonNull WrappedConfirmationType type) {
      WrappedActionButton wrappedYes = type.yesButton();
      WrappedActionButton wrappedNo = type.noButton();
      return new ConfirmationDialog(this.tempBase, this.adaptButton(wrappedYes), this.adaptButton(wrappedNo));
   }

   @NonNull
   public DialogListDialog adaptType(@NonNull WrappedDialogListType type) {
      List<WrappedDialog> wrappedDialogs = type.dialogs();
      WrappedActionButton exitAction = type.exitAction();
      int buttonWidth = type.buttonWidth();
      int columns = type.columns();
      List<Dialog> dialogs = Lists.modify(wrappedDialogs, this::adaptDialog);
      ActionButton exit = exitAction == null ? null : this.adaptButton(exitAction);
      return new DialogListDialog(this.tempBase, dialogs, exit, columns, buttonWidth);
   }

   @NonNull
   public MultiActionDialog adaptType(@NonNull WrappedMultiActionType type) {
      List<WrappedActionButton> wrappedActions = type.actions();
      WrappedActionButton wrappedExit = type.exitAction();
      List<ActionButton> actions = Lists.modify(wrappedActions, this::adaptButton);
      ActionButton exitAction = wrappedExit == null ? null : this.adaptButton(wrappedExit);
      int columns = type.columns();
      return new MultiActionDialog(this.tempBase, actions, columns, exitAction);
   }

   @NonNull
   public NoticeDialog adaptType(@NonNull WrappedNoticeType type) {
      WrappedActionButton wrappedAction = type.action();
      ActionButton action = this.adaptButton(wrappedAction);
      return new NoticeDialog(this.tempBase, action);
   }

   @NonNull
   public ServerLinksDialog adaptType(@NonNull WrappedServerLinksType type) {
      WrappedActionButton wrappedExit = type.exitAction();
      ActionButton exitAction = wrappedExit == null ? null : this.adaptButton(wrappedExit);
      int columns = type.columns();
      int buttonWidth = type.buttonWidth();
      return new ServerLinksDialog(this.tempBase, exitAction, columns, buttonWidth);
   }
}
