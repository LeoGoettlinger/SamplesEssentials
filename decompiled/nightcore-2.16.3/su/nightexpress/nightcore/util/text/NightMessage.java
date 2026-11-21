package su.nightexpress.nightcore.util.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.tag.TagPool;
import su.nightexpress.nightcore.util.text.tag.Tags;

@Deprecated
public class NightMessage {
   @NonNull
   public static TextRoot from(@NonNull String string) {
      return from(string, TagPool.ALL);
   }

   @NonNull
   public static TextRoot from(@NonNull String string, @NonNull TagPool tagPool) {
      return new TextRoot(string, tagPool);
   }

   @NonNull
   public static TextRoot create(@NonNull String string) {
      return create(string, TagPool.ALL);
   }

   @NonNull
   public static TextRoot create(@NonNull String string, @NonNull TagPool tagPool) {
      return from(string, tagPool).compile();
   }

   @NonNull
   public static NightComponent parse(@NonNull String string) {
      return parse(string, TagPool.ALL);
   }

   @NonNull
   public static NightComponent parse(@NonNull String string, @NonNull TagPool tagPool) {
      return from(string, tagPool).parseIfAbsent();
   }

   @Deprecated
   @NonNull
   public static List<String> splitLineTag(@NonNull List<String> list) {
      List<String> segmented = new ArrayList<>();
      list.forEach(line -> Collections.addAll(segmented, Tags.LINE_BREAK.split(line)));
      return segmented;
   }

   @Deprecated
   @NonNull
   public static String clean(@NonNull String string) {
      return create(string, TagPool.NONE).toLegacy();
   }

   @Deprecated
   @NonNull
   public static String stripAll(@NonNull String string) {
      return stripTags(string);
   }

   @NonNull
   public static String stripTags(@NonNull String string) {
      return stripTags(string, TagPool.NONE);
   }

   @NonNull
   public static String stripTags(@NonNull String string, @NonNull TagPool tagPool) {
      return from(string, tagPool).strip();
   }

   @NonNull
   public static String asJson(@NonNull String string) {
      return create(string).toJson();
   }

   @NonNull
   public static String asLegacy(@NonNull String string) {
      return create(string).toLegacy();
   }

   @Deprecated
   public static NightComponent asComponent(@NonNull String string) {
      return create(string).toComponent();
   }

   @Deprecated
   @NonNull
   public static List<String> asLegacy(@NonNull List<String> string) {
      List<String> list = new ArrayList<>();

      for (String str : string) {
         for (String br : Tags.LINE_BREAK.split(str)) {
            list.add(asLegacy(br));
         }
      }

      return list;
   }
}
