package org.skriptlang.skript.bukkit.text;

import ch.njol.skript.Skript;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Inserting;
import net.kyori.adventure.text.minimessage.tag.ParserDirective;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TextComponentParser {
   private static final TextComponentParser INSTANCE = new TextComponentParser();
   private static final Pattern MULTI_WORD_COLOR_PATTERN = Pattern.compile("(\\\\*)<([a-zA-Z]+ [a-zA-Z]+)>");
   private static final Pattern LEGACY_DOUBLE_HASHTAG_PATTERN = Pattern.compile("(\\\\*)<(##[a-fA-F0-9]{6})>");
   static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("(\\\\*)([&§][a-fA-F0-9kKlLoOmMnNrR])");
   private final MiniMessage parser = MiniMessage.builder()
      .strict(false)
      .tags(TagResolver.resolver(new TagResolver[]{StandardTags.defaults(), StandardTags.color(), this.createSkriptTagResolver(false)}))
      .build();
   private final MiniMessage safeParser = MiniMessage.builder().strict(false).tags(TagResolver.resolver(new TagResolver[]{new TagResolver() {
      @Nullable
      public Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
         return TextComponentParser.this.safeTagResolver.resolve(name, arguments, ctx);
      }

      public boolean has(@NotNull String name) {
         return TextComponentParser.this.safeTagResolver.has(name);
      }
   }, this.createSkriptTagResolver(true)})).build();
   private TagResolver safeTagResolver = TagResolver.empty();
   private final Map<String, TextComponentParser.SkriptTag> simplePlaceholders = new HashMap<>();
   private final Set<TextComponentParser.SkriptTagResolver> resolvers = new HashSet<>();
   private TextComponentParser.LinkParseMode linkParseMode = TextComponentParser.LinkParseMode.DISABLED;
   private boolean colorsCauseReset = false;

   public static TextComponentParser instance() {
      return INSTANCE;
   }

   private static Tag disableUnsetStyling(Tag tag) {
      if (tag instanceof Inserting insertingTag) {
         Component content = insertingTag.value();
         HashMap<TextDecoration, State> decorations = new HashMap<>(content.decorations());
         decorations.replaceAll((decoration, state) -> state == State.NOT_SET ? State.FALSE : state);
         content = content.decorations(decorations);
         return insertingTag.allowsChildren() ? Tag.inserting(content) : Tag.selfClosingInserting(content);
      } else {
         return tag;
      }
   }

   TextComponentParser() {
      this.registerCompatibilityTags();
   }

   private TagResolver createSkriptTagResolver(final boolean isSafeMode) {
      return new TagResolver() {
         @Nullable
         public Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
            TextComponentParser.SkriptTag simple = TextComponentParser.this.simplePlaceholders.get(name);
            if (simple != null && this.isSafe(name, simple.parent)) {
               return TextComponentParser.this.colorsCauseReset && simple.reset ? TextComponentParser.disableUnsetStyling(simple.tag) : simple.tag;
            } else {
               for (TextComponentParser.SkriptTagResolver skriptResolver : TextComponentParser.this.resolvers) {
                  if (this.isSafe(name, skriptResolver.parent) && skriptResolver.resolver.has(name)) {
                     return skriptResolver.resolver.resolve(name, arguments, ctx);
                  }
               }

               if (TextComponentParser.this.colorsCauseReset && StandardTags.color().has(name)) {
                  Tag colorTag = StandardTags.color().resolve(name, arguments, ctx);
                  if (colorTag != null) {
                     return TextComponentParser.disableUnsetStyling(colorTag);
                  }
               }

               return null;
            }
         }

         public boolean has(@NotNull String name) {
            TextComponentParser.SkriptTag simple = TextComponentParser.this.simplePlaceholders.get(name);
            if (simple != null) {
               return this.isSafe(name, simple.parent);
            } else {
               for (TextComponentParser.SkriptTagResolver skriptResolver : TextComponentParser.this.resolvers) {
                  if (this.isSafe(name, skriptResolver.parent) && skriptResolver.resolver.has(name)) {
                     return true;
                  }
               }

               return TextComponentParser.this.colorsCauseReset && StandardTags.color().has(name);
            }
         }

         private boolean isSafe(String tag, @Nullable String parent) {
            return !isSafeMode || TextComponentParser.this.safeTagResolver.has(tag) || parent != null && TextComponentParser.this.safeTagResolver.has(parent);
         }
      };
   }

   public void setSafeTags(final String... safeTags) {
      final List<TagResolver> resolvers = new ArrayList<>();
      Lookup lookup = MethodHandles.lookup();

      for (String safeTag : safeTags) {
         try {
            MethodHandle handle = lookup.findStatic(StandardTags.class, safeTag, MethodType.methodType(TagResolver.class));
            resolvers.add((TagResolver)handle.invoke());
         } catch (Throwable var9) {
         }
      }

      this.safeTagResolver = new TagResolver() {
         @Nullable
         public Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
            return this.has(name) ? StandardTags.defaults().resolve(name, arguments, ctx) : null;
         }

         public boolean has(@NotNull String name) {
            return resolvers.stream().anyMatch(resolver -> resolver.has(name)) || CollectionUtils.contains(safeTags, name);
         }
      };
   }

   public void registerPlaceholder(String name, Tag result) {
      this.registerPlaceholder(name, result, null, false);
   }

   public void registerPlaceholder(String name, Tag result, String parent) {
      this.registerPlaceholder(name, result, parent, false);
   }

   public void registerResettingPlaceholder(String name, Tag result) {
      this.registerPlaceholder(name, result, null, true);
   }

   public void registerResettingPlaceholder(String name, Tag result, String parent) {
      this.registerPlaceholder(name, result, parent, true);
   }

   private void registerPlaceholder(String name, Tag result, @Nullable String parent, boolean reset) {
      this.simplePlaceholders.put(name, new TextComponentParser.SkriptTag(result, parent, reset));
   }

   public void unregisterPlaceholder(String tag) {
      this.simplePlaceholders.remove(tag);
   }

   public void registerResolver(TagResolver resolver) {
      this.registerResolver(resolver, null);
   }

   public void registerResolver(TagResolver resolver, String parent) {
      this.resolvers.add(new TextComponentParser.SkriptTagResolver(resolver, parent));
   }

   public void unregisterResolver(TagResolver resolver) {
      this.resolvers.remove(new TextComponentParser.SkriptTagResolver(resolver, null));
   }

   public TextComponentParser.LinkParseMode linkParseMode() {
      return this.linkParseMode;
   }

   public void linkParseMode(TextComponentParser.LinkParseMode linkParseMode) {
      this.linkParseMode = linkParseMode;
   }

   public boolean colorsCauseReset() {
      return this.colorsCauseReset;
   }

   public void colorsCauseReset(boolean colorsCauseReset) {
      this.colorsCauseReset = colorsCauseReset;
   }

   public Component parse(Object message) {
      return this.parse(message, false);
   }

   public Component parseSafe(Object message) {
      return this.parse(message, true);
   }

   private Component parse(Object message, boolean safe) {
      String realMessage = message instanceof String ? (String)message : Classes.toString(message);
      if (realMessage.isEmpty()) {
         return Component.empty();
      } else {
         realMessage = this.reformatText(realMessage);

         Component component;
         try {
            component = safe ? this.safeParser.deserialize(realMessage) : this.parser.deserialize(realMessage);
         } catch (ParsingException var6) {
            Skript.exception(
               var6,
               "An error occurred while trying to parse formatting for '"
                  + realMessage
                  + "'. This is likely caused by the presence of legacy formatting characters (such as '§') that Skript could not handle."
            );
            return Component.text(message instanceof String ? (String)message : Classes.toString(message));
         }

         if (this.linkParseMode != TextComponentParser.LinkParseMode.DISABLED) {
            component = component.replaceText(this.linkParseMode.textReplacementConfig());
         }

         return component;
      }
   }

   public String reformatText(String text) {
      text = MULTI_WORD_COLOR_PATTERN.matcher(text)
         .replaceAll(
            result -> {
               if (result.group(1).length() % 2 == 1) {
                  return Matcher.quoteReplacement(result.group());
               } else {
                  String mappedTag = result.group(2).replace(" ", "_");
                  return !this.simplePlaceholders.containsKey(mappedTag) && !StandardTags.color().has(mappedTag)
                     ? Matcher.quoteReplacement(result.group())
                     : Matcher.quoteReplacement(result.group(1) + "<" + mappedTag + ">");
               }
            }
         );
      text = LEGACY_DOUBLE_HASHTAG_PATTERN.matcher(text).replaceAll(result -> {
         if (result.group(1).length() % 2 == 1) {
            return Matcher.quoteReplacement(result.group());
         } else {
            String mappedTag = result.group(2).substring(1);
            return StandardTags.color().has(mappedTag) ? Matcher.quoteReplacement("<" + mappedTag + ">") : Matcher.quoteReplacement(result.group());
         }
      });
      return TextComponentUtils.replaceLegacyFormattingCodes(text);
   }

   public String escape(String string) {
      if (string.contains("&") || string.contains("§")) {
         string = LEGACY_CODE_PATTERN.matcher(string).replaceAll(result -> {
            String group = result.group().replace('§', '&');
            return result.group(1).length() % 2 == 1 ? Matcher.quoteReplacement(group) : Matcher.quoteReplacement("\\" + group);
         });
      }

      string = LEGACY_DOUBLE_HASHTAG_PATTERN.matcher(string).replaceAll(result -> {
         if (result.group(1).length() % 2 == 1) {
            return Matcher.quoteReplacement(result.group());
         } else {
            String mappedTag = result.group(2).substring(1);
            return StandardTags.color().has(mappedTag) ? Matcher.quoteReplacement("\\<#" + mappedTag) + ">" : Matcher.quoteReplacement(result.group());
         }
      });
      return this.parser.escapeTags(string);
   }

   public String stripFormatting(String string) {
      return this.stripFormatting(string, false);
   }

   public String stripSafeFormatting(String string) {
      return this.stripFormatting(string, true);
   }

   private String stripFormatting(String string, boolean onlySafe) {
      while (true) {
         String stripped = (onlySafe ? this.safeParser : this.parser).stripTags(this.reformatText(string));
         if (string.equals(stripped)) {
            return string;
         }

         string = stripped;
      }
   }

   public String stripFormatting(Component component) {
      return PlainTextComponentSerializer.plainText().serialize(component);
   }

   public String toString(Component component) {
      return (String)this.parser.serialize(component);
   }

   public String toLegacyString(Component component) {
      return LegacyComponentSerializer.legacySection().serialize(component);
   }

   private void registerCompatibilityTags() {
      this.registerResettingPlaceholder("dark_cyan", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.DARK_AQUA}), "dark_aqua");
      this.registerResettingPlaceholder("dark_turquoise", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.DARK_AQUA}), "dark_aqua");
      this.registerResettingPlaceholder("cyan", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.DARK_AQUA}), "dark_aqua");
      this.registerResettingPlaceholder("purple", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.DARK_PURPLE}), "dark_purple");
      this.registerResettingPlaceholder("dark_yellow", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.GOLD}), "gold");
      this.registerResettingPlaceholder("orange", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.GOLD}), "gold");
      this.registerResettingPlaceholder("light_grey", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.GRAY}), "gray");
      this.registerResettingPlaceholder("light_gray", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.GRAY}), "gray");
      this.registerResettingPlaceholder("silver", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.GRAY}), "gray");
      this.registerResettingPlaceholder("dark_silver", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.DARK_GRAY}), "dark_gray");
      this.registerResettingPlaceholder("light_blue", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.BLUE}), "blue");
      this.registerResettingPlaceholder("indigo", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.BLUE}), "blue");
      this.registerResettingPlaceholder("light_green", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.GREEN}), "green");
      this.registerResettingPlaceholder("lime_green", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.GREEN}), "green");
      this.registerResettingPlaceholder("lime", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.GREEN}), "green");
      this.registerResettingPlaceholder("light_cyan", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.AQUA}), "aqua");
      this.registerResettingPlaceholder("light_aqua", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.AQUA}), "aqua");
      this.registerResettingPlaceholder("turquoise", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.AQUA}), "aqua");
      this.registerResettingPlaceholder("light_red", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.RED}), "red");
      this.registerResettingPlaceholder("pink", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.LIGHT_PURPLE}), "light_purple");
      this.registerResettingPlaceholder("magenta", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.LIGHT_PURPLE}), "light_purple");
      this.registerResettingPlaceholder("light_yellow", Tag.styling(new StyleBuilderApplicable[]{NamedTextColor.YELLOW}), "yellow");
      this.registerResettingPlaceholder("brown", Tag.styling(new StyleBuilderApplicable[]{TextColor.color(8606770)}), "color");
      this.registerPlaceholder("magic", Tag.styling(new StyleBuilderApplicable[]{TextDecoration.OBFUSCATED}), "obfuscated");
      this.registerPlaceholder("strike", Tag.styling(new StyleBuilderApplicable[]{TextDecoration.STRIKETHROUGH}), "strikethrough");
      this.registerPlaceholder("s", Tag.styling(new StyleBuilderApplicable[]{TextDecoration.STRIKETHROUGH}), "strikethrough");
      this.registerPlaceholder("underline", Tag.styling(new StyleBuilderApplicable[]{TextDecoration.UNDERLINED}), "underlined");
      this.registerPlaceholder("italics", Tag.styling(new StyleBuilderApplicable[]{TextDecoration.ITALIC}), "italic");
      this.registerPlaceholder("r", ParserDirective.RESET, "reset");
      this.registerResolver(TagResolver.resolver(Set.of("open_url", "link", "url"), (argumentQueue, context) -> {
         String url = argumentQueue.popOr("A link tag must have an argument of the url").value();
         return Tag.styling(new StyleBuilderApplicable[]{ClickEvent.openUrl(url)});
      }), "click");
      this.registerResolver(TagResolver.resolver(Set.of("run_command", "command", "cmd"), (argumentQueue, context) -> {
         String command = argumentQueue.popOr("A run command tag must have an argument of the command to execute").value();
         return Tag.styling(new StyleBuilderApplicable[]{ClickEvent.runCommand(command)});
      }), "click");
      this.registerResolver(TagResolver.resolver(Set.of("suggest_command", "sgt"), (argumentQueue, context) -> {
         String command = argumentQueue.popOr("A suggest command tag must have an argument of the command to suggest").value();
         return Tag.styling(new StyleBuilderApplicable[]{ClickEvent.suggestCommand(command)});
      }), "click");
      this.registerResolver(TagResolver.resolver(Set.of("change_page"), (argumentQueue, context) -> {
         String rawPage = argumentQueue.popOr("A change page tag must have an argument of the page number").value();

         int page;
         try {
            page = Integer.parseInt(rawPage);
         } catch (NumberFormatException var5) {
            throw context.newException(var5.getMessage(), argumentQueue);
         }

         return Tag.styling(new StyleBuilderApplicable[]{ClickEvent.changePage(page)});
      }), "click");
      this.registerResolver(TagResolver.resolver(Set.of("copy_to_clipboard", "copy", "clipboard"), (argumentQueue, context) -> {
         String string = argumentQueue.popOr("A copy to clipboard tag must have an argument of the string to copy").value();
         return Tag.styling(new StyleBuilderApplicable[]{ClickEvent.copyToClipboard(string)});
      }), "click");
      this.registerResolver(TagResolver.resolver(Set.of("show_text", "tooltip", "ttp"), (argumentQueue, context) -> {
         String tooltip = argumentQueue.popOr("A tooltip tag must have an argument of the message to show").value();
         return Tag.styling(new StyleBuilderApplicable[]{HoverEvent.showText(context.deserialize(tooltip))});
      }), "hover");
      this.registerResolver(TagResolver.resolver(Set.of("f"), (argumentQueue, context) -> StandardTags.font().resolve("font", argumentQueue, context)), "font");
      this.registerResolver(
         TagResolver.resolver(Set.of("insertion", "ins"), (argumentQueue, context) -> StandardTags.insertion().resolve("insert", argumentQueue, context)),
         "insert"
      );
      this.registerResolver(
         TagResolver.resolver(Set.of("keybind"), (argumentQueue, context) -> StandardTags.keybind().resolve("key", argumentQueue, context)), "key"
      );
      Pattern unicodePattern = Pattern.compile("[0-9a-f]{4,}");
      this.registerResolver(TagResolver.resolver(Set.of("unicode", "u"), (argumentQueue, context) -> {
         String argument = argumentQueue.popOr("A unicode tag must have an argument of the unicode").value();
         Matcher matcher = unicodePattern.matcher(argument.toLowerCase(Locale.ENGLISH));
         if (!matcher.matches()) {
            throw context.newException("Invalid unicode tag");
         } else {
            String unicode = Character.toString(Integer.parseInt(matcher.group(), 16));
            return Tag.selfClosingInserting(Component.text(unicode));
         }
      }));
   }

   public static enum LinkParseMode {
      DISABLED(null),
      STRICT(
         (TextReplacementConfig)TextReplacementConfig.builder()
            .match(Pattern.compile("https?://[-\\w.]+\\.\\w{2,}(?:/\\S*)?"))
            .replacement(url -> url.clickEvent(ClickEvent.openUrl(url.content())))
            .build()
      ),
      LENIENT(
         (TextReplacementConfig)TextReplacementConfig.builder()
            .match(Pattern.compile("(?:https?://)?[-\\w.]+\\.\\w{2,}(?:/\\S*)?"))
            .replacement(url -> url.clickEvent(ClickEvent.openUrl(url.content())))
            .build()
      );

      private final TextReplacementConfig textReplacementConfig;

      private LinkParseMode(TextReplacementConfig textReplacementConfig) {
         this.textReplacementConfig = textReplacementConfig;
      }

      public TextReplacementConfig textReplacementConfig() {
         return this.textReplacementConfig;
      }
   }

   private record SkriptTag(Tag tag, @Nullable String parent, boolean reset) {
   }

   private record SkriptTagResolver(TagResolver resolver, @Nullable String parent) {
      @Override
      public boolean equals(Object obj) {
         return this.resolver.equals(obj);
      }

      @Override
      public int hashCode() {
         return this.resolver.hashCode();
      }
   }
}
