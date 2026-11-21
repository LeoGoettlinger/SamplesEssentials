package net.kyori.adventure.text.minimessage.internal;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class TagInternals {
   @RegExp
   public static final String TAG_NAME_REGEX = "[!?#]?[a-z0-9_-]*";
   private static final Pattern TAG_NAME_PATTERN = Pattern.compile("[!?#]?[a-z0-9_-]*");

   private TagInternals() {
   }

   public static void assertValidTagName(@TagPattern @NotNull final String tagName) {
      if (!TAG_NAME_PATTERN.matcher(Objects.requireNonNull(tagName)).matches()) {
         throw new IllegalArgumentException("Tag name must match pattern " + TAG_NAME_PATTERN.pattern() + ", was " + tagName);
      }
   }

   public static boolean sanitizeAndCheckValidTagName(@TagPattern @NotNull final String tagName) {
      return TAG_NAME_PATTERN.matcher(Objects.requireNonNull(tagName).toLowerCase(Locale.ROOT)).matches();
   }

   public static void sanitizeAndAssertValidTagName(@TagPattern @NotNull final String tagName) {
      assertValidTagName(Objects.requireNonNull(tagName).toLowerCase(Locale.ROOT));
   }
}
