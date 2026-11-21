package su.nightexpress.nightcore.util.text.night.wrapper;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.bukkit.Material;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.text.event.NightClickEvent;
import su.nightexpress.nightcore.bridge.text.event.NightHoverEvent;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.text.night.ParserUtils;

public class TagWrappers {
   public static final SimpleTagWrapper BOLD = TagWrapper.simple("b");
   public static final SimpleTagWrapper ITALIC = TagWrapper.simple("i");
   public static final SimpleTagWrapper OBFUSCATED = TagWrapper.simple("obf");
   public static final SimpleTagWrapper STRIKETHROUGH = TagWrapper.simple("st");
   public static final SimpleTagWrapper UNDERLINED = TagWrapper.simple("u");
   public static final TagWrappers.OneArgument<SimpleTagWrapper> COLOR = hex -> TagWrapper.withArguments("c", hex);
   public static final TagWrappers.OneArgument<SimpleTagWrapper> SHADOW = hex -> TagWrapper.withArguments("shadow", hex);
   public static final TagWrappers.OneArgument<SimpleTagWrapper> SHADOW_1_0 = hex -> TagWrapper.withArguments("shadow", hex, String.valueOf(1.0F));
   public static final TagWrappers.TwoArgument<SimpleTagWrapper> SHADOW_ALPHA = (hex, alpha) -> TagWrapper.withArguments("shadow", hex, alpha);
   public static final TagWrappers.TwoArgument<SimpleTagWrapper> GRADIENT = (first, second) -> TagWrapper.withArguments("gradient", first, second);
   public static final TagWrappers.ThreeArgument<SimpleTagWrapper> GRADIENT_3 = (first, second, third) -> TagWrapper.withArguments(
      "gradient", first, second, third
   );
   public static final String BR = TagWrapper.simple("br").opening();
   public static final String NEWLINE = TagWrapper.simple("newline").opening();
   public static final TagWrappers.OneArgument<SimpleTagWrapper> FONT = font -> TagWrapper.withArguments("font", font);
   public static final Function<String, String> KEY = key -> TagWrapper.withArguments("key", key).opening();
   public static final TagWrappers.OneArgument<SimpleTagWrapper> INSERTION = text -> TagWrapper.withArguments("insertion", text);
   public static final BiFunction<String, String, String> SPRITE = (atlas, sprite) -> TagWrapper.withArguments(
         "sprite", ParserUtils.quoted(atlas), ParserUtils.quoted(sprite)
      )
      .opening();
   @Deprecated
   public static final Function<String, String> SPRITE_NO_ATLAS = sprite -> TagWrapper.withArguments("sprite", ParserUtils.quoted(sprite)).opening();
   public static final Function<String, String> SPRITE_BLOCKS = sprite -> TagWrapper.withArguments(
         "sprite", ParserUtils.quoted("blocks"), ParserUtils.quoted(sprite)
      )
      .opening();
   public static final Function<String, String> SPRITE_ITEMS = sprite -> TagWrapper.withArguments(
         "sprite", ParserUtils.quoted("items"), ParserUtils.quoted(sprite)
      )
      .opening();
   public static final Function<String, String> SPRITE_GUI = sprite -> TagWrapper.withArguments("sprite", ParserUtils.quoted("gui"), ParserUtils.quoted(sprite))
      .opening();
   public static final Function<Material, String> SPRITE_BLOCK = blockType -> SPRITE_BLOCKS.apply("block/" + BukkitThing.getValue(blockType));
   public static final Function<Material, String> SPRITE_ITEM = itemType -> SPRITE_ITEMS.apply("item/" + BukkitThing.getValue(itemType));
   public static final BiFunction<String, Boolean, String> HEAD = (data, hat) -> TagWrapper.withArguments("head", ParserUtils.quoted(data), String.valueOf(hat))
      .opening();
   public static final Function<String, String> HEAD_HAT = data -> TagWrapper.withArguments("head", ParserUtils.quoted(data)).opening();
   public static final Function<String, String> LANG = key -> TagWrapper.withArguments("lang", ParserUtils.quoted(key)).opening();
   public static final BiFunction<String, String, String> LANG_OR = (key, fallback) -> TagWrapper.withArguments(
         "lang_or", ParserUtils.quoted(key), ParserUtils.quoted(fallback)
      )
      .opening();
   public static final TagWrappers.OneArgument<SimpleTagWrapper> SHOW_TEXT = text -> TagWrapper.withArguments(
      "hover", NightHoverEvent.Action.SHOW_TEXT.name(), ParserUtils.quoted(ParserUtils.escapeQuotes(text))
   );
   public static final TagWrappers.OneArgument<SimpleTagWrapper> SHOW_ITEM = text -> TagWrapper.withArguments(
      "hover", NightHoverEvent.Action.SHOW_ITEM.name(), ParserUtils.quoted(ParserUtils.escapeQuotes(text))
   );
   public static final TagWrappers.OneArgument<SimpleTagWrapper> SUGGEST_COMMAND = command -> TagWrapper.withArguments(
      "click", NightClickEvent.Action.SUGGEST_COMMAND.toString(), ParserUtils.quoted(command)
   );
   public static final TagWrappers.OneArgument<SimpleTagWrapper> RUN_COMMAND = command -> TagWrapper.withArguments(
      "click", NightClickEvent.Action.RUN_COMMAND.toString(), ParserUtils.quoted(command)
   );
   public static final TagWrappers.OneArgument<SimpleTagWrapper> OPEN_FILE = filePath -> TagWrapper.withArguments(
      "click", NightClickEvent.Action.OPEN_FILE.toString(), ParserUtils.quoted(filePath)
   );
   public static final TagWrappers.OneArgument<SimpleTagWrapper> OPEN_URL = url -> TagWrapper.withArguments(
      "click", NightClickEvent.Action.OPEN_URL.toString(), ParserUtils.quoted(url)
   );
   public static final TagWrappers.OneArgument<SimpleTagWrapper> CHANGE_PAGE = page -> TagWrapper.withArguments(
      "click", NightClickEvent.Action.CHANGE_PAGE.toString(), ParserUtils.quoted(page)
   );
   public static final TagWrappers.OneArgument<SimpleTagWrapper> COPY_TO_CLIPBOARD = text -> TagWrapper.withArguments(
      "click", NightClickEvent.Action.COPY_TO_CLIPBOARD.toString(), ParserUtils.quoted(text)
   );
   public static final SimpleTagWrapper RESET = TagWrapper.simple("r");
   public static final SimpleTagWrapper BLACK = TagWrapper.simple("black");
   public static final SimpleTagWrapper WHITE = TagWrapper.simple("white");
   public static final SimpleTagWrapper GRAY = TagWrapper.simple("gray");
   public static final SimpleTagWrapper SOFT_GRAY = TagWrapper.simple("soft_gray");
   public static final SimpleTagWrapper DARK_GRAY = TagWrapper.simple("dark_gray");
   public static final SimpleTagWrapper RED = TagWrapper.simple("red");
   public static final SimpleTagWrapper SOFT_RED = TagWrapper.simple("soft_red");
   public static final SimpleTagWrapper DARK_RED = TagWrapper.simple("dark_red");
   public static final SimpleTagWrapper GREEN = TagWrapper.simple("green");
   public static final SimpleTagWrapper SOFT_GREEN = TagWrapper.simple("soft_green");
   public static final SimpleTagWrapper DARK_GREEN = TagWrapper.simple("dark_green");
   public static final SimpleTagWrapper BLUE = TagWrapper.simple("blue");
   public static final SimpleTagWrapper SOFT_BLUE = TagWrapper.simple("soft_blue");
   public static final SimpleTagWrapper DARK_BLUE = TagWrapper.simple("dark_blue");
   public static final SimpleTagWrapper YELLOW = TagWrapper.simple("yellow");
   public static final SimpleTagWrapper SOFT_YELLOW = TagWrapper.simple("soft_yellow");
   public static final SimpleTagWrapper DARK_YELLOW = TagWrapper.simple("dark_yellow");
   public static final SimpleTagWrapper ORANGE = TagWrapper.simple("orange");
   public static final SimpleTagWrapper SOFT_ORANGE = TagWrapper.simple("soft_orange");
   public static final SimpleTagWrapper GOLD = TagWrapper.simple("gold");
   public static final SimpleTagWrapper AQUA = TagWrapper.simple("aqua");
   public static final SimpleTagWrapper SOFT_AQUA = TagWrapper.simple("soft_aqua");
   public static final SimpleTagWrapper DARK_AQUA = TagWrapper.simple("dark_aqua");
   public static final SimpleTagWrapper PURPLE = TagWrapper.simple("purple");
   public static final SimpleTagWrapper SOFT_PURPLE = TagWrapper.simple("soft_purple");
   public static final SimpleTagWrapper LIGHT_PURPLE = TagWrapper.simple("light_purple");
   public static final SimpleTagWrapper DARK_PURPLE = TagWrapper.simple("dark_purple");
   public static final SimpleTagWrapper PINK = TagWrapper.simple("pink");
   public static final SimpleTagWrapper SOFT_PINK = TagWrapper.simple("soft_pink");

   public interface OneArgument<T extends TagWrapper> {
      @NonNull
      T with(@NonNull String var1);
   }

   public interface ThreeArgument<T extends TagWrapper> {
      @NonNull
      T with(@NonNull String var1, @NonNull String var2, @NonNull String var3);
   }

   public interface TwoArgument<T extends TagWrapper> {
      @NonNull
      T with(@NonNull String var1, @NonNull String var2);
   }
}
