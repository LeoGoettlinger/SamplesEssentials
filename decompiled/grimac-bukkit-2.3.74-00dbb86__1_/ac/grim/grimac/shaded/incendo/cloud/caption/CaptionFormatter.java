package ac.grim.grimac.shaded.incendo.cloud.caption;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface CaptionFormatter<C, T> {
   @NonNull
   static <C> CaptionFormatter<C, String> patternReplacing(@NonNull final Pattern pattern) {
      return new CaptionFormatter.PatternReplacingCaptionFormatter<>(pattern);
   }

   @NonNull
   static <C> CaptionFormatter<C, String> placeholderReplacing() {
      return new CaptionFormatter.PatternReplacingCaptionFormatter<>(placeholderPattern());
   }

   static Pattern placeholderPattern() {
      return Pattern.compile("<(\\S+)>");
   }

   @NonNull
   default T formatCaption(@NonNull Caption captionKey, @NonNull C recipient, @NonNull String caption, @NonNull CaptionVariable... variables) {
      return this.formatCaption(captionKey, recipient, caption, Arrays.asList(variables));
   }

   @NonNull
   T formatCaption(@NonNull Caption captionKey, @NonNull C recipient, @NonNull String caption, @NonNull List<CaptionVariable> variables);

   public static final class PatternReplacingCaptionFormatter<C> implements CaptionFormatter<C, String> {
      private final Pattern pattern;

      private PatternReplacingCaptionFormatter(@NonNull final Pattern pattern) {
         this.pattern = pattern;
      }

      @NonNull
      public String formatCaption(
         @NonNull final Caption captionKey, @NonNull final C recipient, @NonNull final String caption, @NonNull final List<CaptionVariable> variables
      ) {
         Map<String, String> replacements = new HashMap<>();

         for (CaptionVariable variable : variables) {
            replacements.put(variable.key(), variable.value());
         }

         Matcher matcher = this.pattern.matcher(caption);
         StringBuffer stringBuffer = new StringBuffer();

         while (matcher.find()) {
            String replacement = replacements.get(matcher.group(1));
            matcher.appendReplacement(stringBuffer, replacement == null ? "$0" : replacement);
         }

         matcher.appendTail(stringBuffer);
         return stringBuffer.toString();
      }
   }
}
