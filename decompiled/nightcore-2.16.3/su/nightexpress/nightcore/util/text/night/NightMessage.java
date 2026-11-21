package su.nightexpress.nightcore.util.text.night;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.night.tag.TagPool;

public class NightMessage {
   @NonNull
   public static NightComponent parse(@NonNull String string) {
      return TextParser.parse(string);
   }

   @NonNull
   public static NightComponent parse(@NonNull String string, @NonNull TagPool tagPool) {
      return TextParser.parse(string, tagPool);
   }

   @NonNull
   public static String stripTags(@NonNull String string) {
      return stripTags(string, TagPool.NONE);
   }

   @NonNull
   public static String stripTags(@NonNull String string, @NonNull TagPool tagPool) {
      return TextParser.strip(string, tagPool);
   }

   @NonNull
   public static String asJson(@NonNull String string) {
      return parse(string).toJson();
   }

   @NonNull
   public static String asLegacy(@NonNull String string) {
      return parse(string).toLegacy();
   }
}
