package su.nightexpress.nightcore.locale.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.ConfigType;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.LangValue;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

public class DialogElementLocale extends LangEntry<DialogElementLocale.Value> {
   private static final ConfigType<DialogElementLocale.Value> CONFIG_TYPE = ConfigType.of(DialogElementLocale.Value::read, FileConfig::set);

   public DialogElementLocale(@NonNull String path, @NonNull DialogElementLocale.Value defaultValue) {
      super(CONFIG_TYPE, path, defaultValue);
   }

   @NonNull
   public static DialogElementLocale create(@NonNull String path, String... contents) {
      return create(path, 200, contents);
   }

   @NonNull
   public static DialogElementLocale create(@NonNull String path, int width, String... contents) {
      return new DialogElementLocale(path, new DialogElementLocale.Value(String.join(TagWrappers.BR, contents), width));
   }

   @NonNull
   public DialogElementLocale replace(@NonNull UnaryOperator<String> operator) {
      return create(this.path, this.width(), operator.apply(this.contents()));
   }

   @NonNull
   public DialogElementLocale replace(@NonNull PlaceholderContext context) {
      return this.replace(context::apply);
   }

   @NonNull
   public String contents() {
      return this.value.contents();
   }

   public int width() {
      return this.value.width();
   }

   public record Value(@NonNull String contents, int width) implements LangValue {
      @NonNull
      public static DialogElementLocale.Value read(@NonNull FileConfig config, @NonNull String path) {
         List<String> list = config.getStringList(path + ".Contents");
         String contents;
         if (!list.isEmpty()) {
            contents = String.join(TagWrappers.BR, list);
         } else {
            contents = String.valueOf(config.getString(path + ".Contents"));
         }

         int width = config.getInt(path + ".Width");
         return new DialogElementLocale.Value(contents, width);
      }

      @Override
      public void write(@NonNull FileConfig config, @NonNull String path) {
         List<String> list = new ArrayList<>();
         StringUtil.splitDelimiters(this.contents, list::add);
         config.set(path + ".Contents", list.size() != 1 ? list : list.getFirst());
         config.set(path + ".Width", this.width);
      }
   }
}
