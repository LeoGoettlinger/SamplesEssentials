package su.nightexpress.nightcore.language.tag;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.language.message.MessageOptions;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.tag.TagUtils;
import su.nightexpress.nightcore.util.text.tag.api.Tag;

@Deprecated
public abstract class MessageTag extends Tag {
   public MessageTag(@NonNull String name) {
      super(name);
   }

   public MessageTag(@NonNull String name, String[] aliases) {
      super(name, aliases);
   }

   @Deprecated
   @NonNull
   public String enclose(@NonNull String content) {
      return this.wrap(content);
   }

   @NonNull
   public String wrap(@NonNull String content) {
      return TagUtils.brackets(this.getName() + ":" + ParserUtils.quoted(content));
   }

   public abstract void apply(@NonNull MessageOptions var1, @Nullable String var2);
}
