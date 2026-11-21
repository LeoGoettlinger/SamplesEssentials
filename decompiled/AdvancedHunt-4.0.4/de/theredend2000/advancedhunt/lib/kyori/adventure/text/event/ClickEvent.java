package de.theredend2000.advancedhunt.lib.kyori.adventure.text.event;

import de.theredend2000.advancedhunt.lib.kyori.adventure.audience.Audience;
import de.theredend2000.advancedhunt.lib.kyori.adventure.builder.AbstractBuilder;
import de.theredend2000.advancedhunt.lib.kyori.adventure.dialog.DialogLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Keyed;
import de.theredend2000.advancedhunt.lib.kyori.adventure.nbt.api.BinaryTagHolder;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.StyleBuilderApplicable;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.Index;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public final class ClickEvent<T extends ClickEvent.Payload> implements StyleBuilderApplicable {
   private final ClickEvent.Action<T> action;
   private final ClickEvent.Payload payload;

   public static ClickEvent<ClickEvent.Payload.Text> openUrl(final String url) {
      return clickEvent(ClickEvent.Action.OPEN_URL, ClickEvent.Payload.string(url));
   }

   public static ClickEvent<ClickEvent.Payload.Text> openUrl(final URL url) {
      return openUrl(url.toExternalForm());
   }

   public static ClickEvent<ClickEvent.Payload.Text> openFile(final String file) {
      return clickEvent(ClickEvent.Action.OPEN_FILE, ClickEvent.Payload.string(file));
   }

   public static ClickEvent<ClickEvent.Payload.Text> runCommand(final String command) {
      return clickEvent(ClickEvent.Action.RUN_COMMAND, ClickEvent.Payload.string(command));
   }

   public static ClickEvent<ClickEvent.Payload.Text> suggestCommand(final String command) {
      return clickEvent(ClickEvent.Action.SUGGEST_COMMAND, ClickEvent.Payload.string(command));
   }

   public static ClickEvent<ClickEvent.Payload.Int> changePage(final int page) {
      return clickEvent(ClickEvent.Action.CHANGE_PAGE, ClickEvent.Payload.integer(page));
   }

   public static ClickEvent<ClickEvent.Payload.Text> copyToClipboard(final String text) {
      return clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ClickEvent.Payload.string(text));
   }

   public static ClickEvent<?> callback(final ClickCallback<Audience> function) {
      return ClickCallbackInternals.PROVIDER.create(Objects.requireNonNull(function, "function"), ClickCallbackOptionsImpl.DEFAULT);
   }

   public static ClickEvent<?> callback(final ClickCallback<Audience> function, final ClickCallback.Options options) {
      return ClickCallbackInternals.PROVIDER.create(Objects.requireNonNull(function, "function"), Objects.requireNonNull(options, "options"));
   }

   public static ClickEvent<?> callback(final ClickCallback<Audience> function, final Consumer<ClickCallback.Options.Builder> optionsBuilder) {
      return ClickCallbackInternals.PROVIDER
         .create(
            Objects.requireNonNull(function, "function"),
            AbstractBuilder.configureAndBuild(ClickCallback.Options.builder(), Objects.requireNonNull(optionsBuilder, "optionsBuilder"))
         );
   }

   public static ClickEvent<ClickEvent.Payload.Dialog> showDialog(final DialogLike dialog) {
      return clickEvent(ClickEvent.Action.SHOW_DIALOG, ClickEvent.Payload.dialog(dialog));
   }

   public static ClickEvent<ClickEvent.Payload.Custom> custom(final Key key) {
      return custom(key, null);
   }

   public static ClickEvent<ClickEvent.Payload.Custom> custom(final Key key, @Nullable final BinaryTagHolder nbt) {
      return clickEvent(ClickEvent.Action.CUSTOM, ClickEvent.Payload.custom(key, nbt));
   }

   public static <T extends ClickEvent.Payload> ClickEvent<T> clickEvent(final ClickEvent.Action<T> action, final T payload) {
      Objects.requireNonNull(action, "action");
      Objects.requireNonNull(payload, "payload");
      action.validate(payload);
      return new ClickEvent<>(action, payload);
   }

   private ClickEvent(final ClickEvent.Action<T> action, final ClickEvent.Payload payload) {
      this.action = action;
      this.payload = payload;
   }

   public ClickEvent.Action<T> action() {
      return this.action;
   }

   public ClickEvent.Payload payload() {
      return this.payload;
   }

   @Override
   public void styleApply(final Style.Builder style) {
      style.clickEvent(this);
   }

   @Override
   public boolean equals(final Object o) {
      return !(o instanceof ClickEvent<?> that) ? false : this.action.equals(that.action) && this.payload.equals(that.payload);
   }

   @Override
   public int hashCode() {
      int result = this.action.hashCode();
      return 31 * result + this.payload.hashCode();
   }

   @Override
   public String toString() {
      return "ClickEvent{action=" + this.action + ", payload=" + this.payload + "}";
   }

   public abstract static sealed class Action<T extends ClickEvent.Payload>
      permits ClickEvent.Action.ChangePage,
      ClickEvent.Action.Custom,
      ClickEvent.Action.ShowDialog,
      ClickEvent.Action.TextCarrier {
      public static final ClickEvent.Action.OpenUrl OPEN_URL = new ClickEvent.Action.OpenUrl();
      public static final ClickEvent.Action.OpenFile OPEN_FILE = new ClickEvent.Action.OpenFile();
      public static final ClickEvent.Action.RunCommand RUN_COMMAND = new ClickEvent.Action.RunCommand();
      public static final ClickEvent.Action.SuggestCommand SUGGEST_COMMAND = new ClickEvent.Action.SuggestCommand();
      public static final ClickEvent.Action.ChangePage CHANGE_PAGE = new ClickEvent.Action.ChangePage();
      public static final ClickEvent.Action.CopyToClipboard COPY_TO_CLIPBOARD = new ClickEvent.Action.CopyToClipboard();
      public static final ClickEvent.Action.ShowDialog SHOW_DIALOG = new ClickEvent.Action.ShowDialog();
      public static final ClickEvent.Action.Custom CUSTOM = new ClickEvent.Action.Custom();
      public static final Index<String, ClickEvent.Action<?>> NAMES = Index.create(
         ClickEvent.Action::toString, OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, CHANGE_PAGE, COPY_TO_CLIPBOARD, SHOW_DIALOG, CUSTOM
      );
      private final String name;
      private final boolean readable;
      private final Class<? extends ClickEvent.Payload> payloadType;

      Action(final String name, final boolean readable, final Class<? extends ClickEvent.Payload> payloadType) {
         this.name = name;
         this.readable = readable;
         this.payloadType = payloadType;
      }

      public boolean readable() {
         return this.readable;
      }

      public boolean supports(final ClickEvent.Payload payload) {
         return this.payloadType.isAssignableFrom(payload.getClass());
      }

      public boolean isValid(final T payload) {
         try {
            this.validate(payload);
            return true;
         } catch (IllegalArgumentException var3) {
            return false;
         }
      }

      public void validate(final T payload) throws IllegalArgumentException {
      }

      public String name() {
         return this.name;
      }

      @Override
      public String toString() {
         return this.name;
      }

      @Override
      public int hashCode() {
         return this.name.hashCode();
      }

      @Override
      public boolean equals(final Object obj) {
         return obj == this;
      }

      public static final class ChangePage extends ClickEvent.Action<ClickEvent.Payload.Int> {
         private ChangePage() {
            super("change_page", true, ClickEvent.Payload.Int.class);
         }

         public void validate(final ClickEvent.Payload.Int payload) throws IllegalArgumentException {
            int page = payload.integer();
            if (page < 1) {
               throw new IllegalArgumentException("Change page payload integer must be greater than or equal to 1, was " + page);
            }
         }
      }

      public static final class CopyToClipboard extends ClickEvent.Action.TextCarrier {
         private CopyToClipboard() {
            super("copy_to_clipboard", true);
         }
      }

      public static final class Custom extends ClickEvent.Action<ClickEvent.Payload.Custom> {
         private Custom() {
            super("custom", true, ClickEvent.Payload.Custom.class);
         }
      }

      public static final class OpenFile extends ClickEvent.Action.TextCarrier {
         private OpenFile() {
            super("open_file", false);
         }
      }

      public static final class OpenUrl extends ClickEvent.Action.TextCarrier {
         private OpenUrl() {
            super("open_url", true);
         }

         public void validate(final ClickEvent.Payload.Text payload) throws IllegalArgumentException {
            try {
               new URI(payload.value());
            } catch (URISyntaxException var3) {
               throw new IllegalArgumentException("Syntax exception in payload value: " + payload.value(), var3);
            }
         }
      }

      public static final class RunCommand extends ClickEvent.Action.TextCarrier {
         private RunCommand() {
            super("run_command", true);
         }
      }

      public static final class ShowDialog extends ClickEvent.Action<ClickEvent.Payload.Dialog> {
         private ShowDialog() {
            super("show_dialog", false, ClickEvent.Payload.Dialog.class);
         }
      }

      public static final class SuggestCommand extends ClickEvent.Action.TextCarrier {
         private SuggestCommand() {
            super("suggest_command", true);
         }
      }

      public abstract static sealed class TextCarrier
         extends ClickEvent.Action<ClickEvent.Payload.Text>
         permits ClickEvent.Action.OpenUrl,
         ClickEvent.Action.OpenFile,
         ClickEvent.Action.RunCommand,
         ClickEvent.Action.SuggestCommand,
         ClickEvent.Action.CopyToClipboard {
         TextCarrier(final String name, final boolean readable) {
            super(name, readable, ClickEvent.Payload.Text.class);
         }
      }
   }

   public sealed interface Payload permits ClickEvent.Payload.Custom, ClickEvent.Payload.Dialog, ClickEvent.Payload.Int, ClickEvent.Payload.Text {
      static ClickEvent.Payload.Text string(final String value) {
         Objects.requireNonNull(value, "value");
         return new PayloadImpl.TextImpl(value);
      }

      static ClickEvent.Payload.Int integer(final int integer) {
         return new PayloadImpl.IntImpl(integer);
      }

      static ClickEvent.Payload.Dialog dialog(final DialogLike dialog) {
         Objects.requireNonNull(dialog, "dialog");
         return new PayloadImpl.DialogImpl(dialog);
      }

      static ClickEvent.Payload.Custom custom(final Key key) {
         return custom(key, null);
      }

      static ClickEvent.Payload.Custom custom(final Key key, @Nullable final BinaryTagHolder nbt) {
         Objects.requireNonNull(key, "key");
         return new PayloadImpl.CustomImpl(key, nbt);
      }

      public sealed interface Custom extends ClickEvent.Payload, Keyed permits PayloadImpl.CustomImpl {
         @Nullable
         BinaryTagHolder nbt();
      }

      public sealed interface Dialog extends ClickEvent.Payload permits PayloadImpl.DialogImpl {
         DialogLike dialog();
      }

      public sealed interface Int extends ClickEvent.Payload permits PayloadImpl.IntImpl {
         int integer();
      }

      public sealed interface Text extends ClickEvent.Payload permits PayloadImpl.TextImpl {
         String value();
      }
   }
}
