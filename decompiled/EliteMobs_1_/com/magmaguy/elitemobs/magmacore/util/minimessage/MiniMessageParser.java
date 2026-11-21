package com.magmaguy.elitemobs.magmacore.util.minimessage;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;

public final class MiniMessageParser {
   private static final Map<String, ChatColor> NAMED_COLORS = Map.ofEntries(
      Map.entry("black", ChatColor.BLACK),
      Map.entry("dark_blue", ChatColor.DARK_BLUE),
      Map.entry("dark_green", ChatColor.DARK_GREEN),
      Map.entry("dark_aqua", ChatColor.DARK_AQUA),
      Map.entry("dark_red", ChatColor.DARK_RED),
      Map.entry("dark_purple", ChatColor.DARK_PURPLE),
      Map.entry("gold", ChatColor.GOLD),
      Map.entry("gray", ChatColor.GRAY),
      Map.entry("dark_gray", ChatColor.DARK_GRAY),
      Map.entry("blue", ChatColor.BLUE),
      Map.entry("green", ChatColor.GREEN),
      Map.entry("aqua", ChatColor.AQUA),
      Map.entry("red", ChatColor.RED),
      Map.entry("light_purple", ChatColor.LIGHT_PURPLE),
      Map.entry("yellow", ChatColor.YELLOW),
      Map.entry("white", ChatColor.WHITE)
   );
   private static final Map<String, String> DECORATIONS = Map.ofEntries(
      Map.entry("bold", "bold"),
      Map.entry("b", "bold"),
      Map.entry("italic", "italic"),
      Map.entry("i", "italic"),
      Map.entry("em", "italic"),
      Map.entry("underlined", "underlined"),
      Map.entry("u", "underlined"),
      Map.entry("strikethrough", "strikethrough"),
      Map.entry("st", "strikethrough"),
      Map.entry("obfuscated", "obfuscated"),
      Map.entry("obf", "obfuscated")
   );
   private static final Set<String> OTHER_TAGS = Set.of(
      "color",
      "colour",
      "c",
      "reset",
      "gradient",
      "g",
      "rainbow",
      "r",
      "transition",
      "pride",
      "newline",
      "br",
      "hover",
      "click",
      "insert",
      "insertion",
      "font",
      "key",
      "lang",
      "tr",
      "translate",
      "lang_or",
      "tr_or",
      "translate_or",
      "nbt",
      "data",
      "score",
      "selector",
      "sel",
      "shadow",
      "sprite",
      "head"
   );

   private MiniMessageParser() {
   }

   public static BaseComponent[] parse(String input) {
      if (input != null && !input.isEmpty()) {
         List<MiniMessageParser.Token> tokens = tokenize(applyLegacy(input));
         MiniMessageParser.ElementNode root = buildTree(tokens);
         List<BaseComponent> out = new ArrayList<>();

         for (MiniMessageParser.Node child : root.children) {
            render(child, new MiniMessageParser.Style(), out);
         }

         if (out.isEmpty()) {
            out.add(new TextComponent(""));
         }

         return out.toArray(new BaseComponent[0]);
      } else {
         return new BaseComponent[]{new TextComponent("")};
      }
   }

   public static String toLegacy(String input) {
      if (input == null) {
         return "";
      } else if (!containsFormatting(input)) {
         return input;
      } else {
         BaseComponent[] parsed = parse(input);

         try {
            return BaseComponent.toLegacyText(parsed);
         } catch (RuntimeException var10) {
            StringBuilder sb = new StringBuilder();

            for (BaseComponent c : parsed) {
               try {
                  sb.append(BaseComponent.toLegacyText(new BaseComponent[]{c}));
               } catch (RuntimeException var9) {
               }
            }

            return sb.toString();
         }
      }
   }

   private static boolean containsFormatting(String input) {
      for (int i = 0; i < input.length(); i++) {
         char c = input.charAt(i);
         if ((c == '&' || c == 167) && i + 1 < input.length()) {
            char n = Character.toLowerCase(input.charAt(i + 1));
            if (n == '#' && i + 8 <= input.length() && isHex6(input, i + 2)) {
               return true;
            }

            if (readSpreadHex(input, i) != null) {
               return true;
            }

            if (legacyCodeToTag(n) != null) {
               return true;
            }
         } else if (c == '<') {
            int end = findTagEnd(input, i);
            if (end > 0 && parseTag(input.substring(i + 1, end)) != null) {
               return true;
            }
         }
      }

      return false;
   }

   static String applyLegacy(String s) {
      StringBuilder out = new StringBuilder(s.length() + 16);
      int i = 0;
      int len = s.length();

      while (i < len) {
         char c = s.charAt(i);
         if ((c == '&' || c == 167) && i + 1 < len) {
            char n = Character.toLowerCase(s.charAt(i + 1));
            if (n == '#' && i + 8 <= len && isHex6(s, i + 2)) {
               out.append("<reset><#").append(s, i + 2, i + 8).append('>');
               i += 8;
               continue;
            }

            String spread = readSpreadHex(s, i);
            if (spread != null) {
               out.append("<reset><#").append(spread).append('>');
               i += 14;
               continue;
            }

            String tag = legacyCodeToTag(n);
            if (tag != null) {
               out.append(tag);
               i += 2;
               continue;
            }
         }

         out.append(c);
         i++;
      }

      return out.toString();
   }

   private static boolean isHex6(String s, int from) {
      if (from + 6 > s.length()) {
         return false;
      } else {
         for (int i = from; i < from + 6; i++) {
            if (Character.digit(s.charAt(i), 16) < 0) {
               return false;
            }
         }

         return true;
      }
   }

   private static String readSpreadHex(String s, int start) {
      if (start + 14 > s.length()) {
         return null;
      } else if (Character.toLowerCase(s.charAt(start + 1)) != 'x') {
         return null;
      } else {
         StringBuilder hex = new StringBuilder(6);

         for (int k = 0; k < 6; k++) {
            char marker = s.charAt(start + 2 + k * 2);
            char digit = s.charAt(start + 3 + k * 2);
            if (marker != '&' && marker != 167) {
               return null;
            }

            if (Character.digit(digit, 16) < 0) {
               return null;
            }

            hex.append(digit);
         }

         return hex.toString();
      }
   }

   private static String legacyCodeToTag(char code) {
      return switch (code) {
         case '0' -> "<reset><black>";
         case '1' -> "<reset><dark_blue>";
         case '2' -> "<reset><dark_green>";
         case '3' -> "<reset><dark_aqua>";
         case '4' -> "<reset><dark_red>";
         case '5' -> "<reset><dark_purple>";
         case '6' -> "<reset><gold>";
         case '7' -> "<reset><gray>";
         case '8' -> "<reset><dark_gray>";
         case '9' -> "<reset><blue>";
         default -> null;
         case 'a' -> "<reset><green>";
         case 'b' -> "<reset><aqua>";
         case 'c' -> "<reset><red>";
         case 'd' -> "<reset><light_purple>";
         case 'e' -> "<reset><yellow>";
         case 'f' -> "<reset><white>";
         case 'k' -> "<obfuscated>";
         case 'l' -> "<bold>";
         case 'm' -> "<strikethrough>";
         case 'n' -> "<underlined>";
         case 'o' -> "<italic>";
         case 'r' -> "<reset>";
      };
   }

   private static boolean isKnownTagName(String name) {
      if (name.startsWith("!")) {
         name = name.substring(1);
      }

      return NAMED_COLORS.containsKey(name) || DECORATIONS.containsKey(name) || OTHER_TAGS.contains(name) || isHex(name);
   }

   private static boolean isColorTag(String name) {
      return NAMED_COLORS.containsKey(name) || isHex(name) || name.equals("color") || name.equals("colour") || name.equals("c");
   }

   private static boolean isHex(String s) {
      if (s.length() == 7 && s.charAt(0) == '#') {
         for (int i = 1; i < 7; i++) {
            if (Character.digit(s.charAt(i), 16) < 0) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static List<MiniMessageParser.Token> tokenize(String s) {
      List<MiniMessageParser.Token> tokens = new ArrayList<>();
      StringBuilder text = new StringBuilder();
      int i = 0;
      int len = s.length();

      while (i < len) {
         char c = s.charAt(i);
         if (c == '\\' && i + 1 < len) {
            char next = s.charAt(i + 1);
            if (next == '<' || next == '\\') {
               text.append(next);
               i += 2;
               continue;
            }
         }

         if (c == '<') {
            int end = findTagEnd(s, i);
            if (end > 0) {
               MiniMessageParser.Token tag = parseTag(s.substring(i + 1, end));
               if (tag != null) {
                  if (text.length() > 0) {
                     tokens.add(new MiniMessageParser.TextTok(text.toString()));
                     text.setLength(0);
                  }

                  tokens.add(tag);
                  i = end + 1;
                  continue;
               }
            }
         }

         text.append(c);
         i++;
      }

      if (text.length() > 0) {
         tokens.add(new MiniMessageParser.TextTok(text.toString()));
      }

      return tokens;
   }

   private static int findTagEnd(String s, int start) {
      char quote = 0;

      for (int j = start + 1; j < s.length(); j++) {
         char c = s.charAt(j);
         if (quote != 0) {
            if (c == '\\') {
               j++;
            } else if (c == quote) {
               quote = 0;
            }
         } else if (c != '\'' && c != '"') {
            if (c == '<') {
               return -1;
            }

            if (c == '>') {
               return j;
            }
         } else {
            quote = c;
         }
      }

      return -1;
   }

   private static MiniMessageParser.Token parseTag(String raw) {
      if (raw.isEmpty()) {
         return null;
      } else {
         boolean selfClose = false;
         if (raw.endsWith("/")) {
            selfClose = true;
            raw = raw.substring(0, raw.length() - 1);
         }

         if (raw.startsWith("/")) {
            String name = raw.substring(1).toLowerCase(Locale.ROOT);
            return isValidName(name) && isKnownTagName(name) ? new MiniMessageParser.CloseTok(name) : null;
         } else {
            List<String> parts = splitTopLevel(raw, ':');
            if (parts.isEmpty()) {
               return null;
            } else {
               String name = parts.get(0).toLowerCase(Locale.ROOT);
               if (isValidName(name) && isKnownTagName(name)) {
                  List<String> args = new ArrayList<>();

                  for (int k = 1; k < parts.size(); k++) {
                     args.add(unquote(parts.get(k)));
                  }

                  return new MiniMessageParser.OpenTok(name, args, selfClose);
               } else {
                  return null;
               }
            }
         }
      }
   }

   private static boolean isValidName(String name) {
      if (name.isEmpty()) {
         return false;
      } else {
         int start = name.charAt(0) == '!' ? 1 : 0;
         if (start == name.length()) {
            return false;
         } else {
            for (int i = start; i < name.length(); i++) {
               char c = name.charAt(i);
               if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != '#') {
                  return false;
               }
            }

            return true;
         }
      }
   }

   private static List<String> splitTopLevel(String s, char delim) {
      List<String> out = new ArrayList<>();
      StringBuilder cur = new StringBuilder();
      char quote = 0;

      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (quote != 0) {
            if (c == '\\' && i + 1 < s.length()) {
               cur.append(c).append(s.charAt(++i));
            } else {
               cur.append(c);
               if (c == quote) {
                  quote = 0;
               }
            }
         } else if (c == '\'' || c == '"') {
            quote = c;
            cur.append(c);
         } else if (c == delim) {
            out.add(cur.toString());
            cur.setLength(0);
         } else {
            cur.append(c);
         }
      }

      out.add(cur.toString());
      return out;
   }

   private static String unquote(String s) {
      if (s.length() >= 2 && (s.charAt(0) == '\'' || s.charAt(0) == '"') && s.charAt(s.length() - 1) == s.charAt(0)) {
         String inner = s.substring(1, s.length() - 1);
         return inner.replace("\\'", "'").replace("\\\"", "\"").replace("\\\\", "\\");
      } else {
         return s;
      }
   }

   private static boolean isVoid(String name) {
      return switch (name) {
         case "newline", "br", "key", "lang", "tr", "translate", "lang_or", "tr_or", "translate_or", "nbt", "data", "score", "selector", "sel", "sprite", "head" -> true;
         default -> false;
      };
   }

   private static MiniMessageParser.ElementNode buildTree(List<MiniMessageParser.Token> tokens) {
      MiniMessageParser.ElementNode root = new MiniMessageParser.ElementNode("root", List.of());
      Deque<MiniMessageParser.ElementNode> stack = new ArrayDeque<>();
      stack.push(root);

      for (MiniMessageParser.Token t : tokens) {
         if (t instanceof MiniMessageParser.TextTok tt) {
            stack.peek().children.add(new MiniMessageParser.TextNode(tt.text()));
         } else if (t instanceof MiniMessageParser.OpenTok ot) {
            if (ot.name().equals("reset")) {
               while (stack.size() > 1) {
                  stack.pop();
               }
            } else {
               MiniMessageParser.ElementNode el = new MiniMessageParser.ElementNode(ot.name(), ot.args());
               stack.peek().children.add(el);
               if (!ot.selfClose() && !isVoid(ot.name())) {
                  stack.push(el);
               }
            }
         } else if (t instanceof MiniMessageParser.CloseTok ct && stack.stream().anyMatch(e -> e.name.equals(ct.name()))) {
            while (stack.size() > 1 && !stack.peek().name.equals(ct.name())) {
               stack.pop();
            }

            if (stack.size() > 1) {
               stack.pop();
            }
         }
      }

      return root;
   }

   private static void render(MiniMessageParser.Node node, MiniMessageParser.Style style, List<BaseComponent> out) {
      if (node instanceof MiniMessageParser.TextNode tn) {
         emit(tn.text(), style, out);
      } else {
         MiniMessageParser.ElementNode el = (MiniMessageParser.ElementNode)node;
         String var4 = el.name;
         switch (var4) {
            case "newline":
            case "br":
               emit("\n", style, out);
            case "reset":
            case "nbt":
            case "data":
            case "score":
            case "selector":
            case "sel":
            case "sprite":
            case "head":
               break;
            case "gradient":
            case "g":
               renderGradient(el, parseColors(el.args), style, out);
               break;
            case "pride":
               renderGradient(el, prideColors(el.args), style, out);
               break;
            case "transition":
               renderTransition(el, parseColors(el.args), style, out);
               break;
            case "rainbow":
            case "r":
               renderRainbow(el, style, out);
               break;
            case "key":
               if (!el.args.isEmpty()) {
                  emitComponent(new KeybindComponent(el.args.get(0)), style, out);
               }
               break;
            case "lang":
            case "tr":
            case "translate":
            case "lang_or":
            case "tr_or":
            case "translate_or":
               if (!el.args.isEmpty()) {
                  TranslatableComponent tc = new TranslatableComponent(el.args.get(0), new Object[0]);
                  List<BaseComponent> with = new ArrayList<>();

                  for (int k = 1; k < el.args.size(); k++) {
                     with.add(new TextComponent(parse(el.args.get(k))));
                  }

                  tc.setWith(with);
                  emitComponent(tc, style, out);
               }
               break;
            case "shadow":
               for (MiniMessageParser.Node child : el.children) {
                  render(child, style, out);
               }
               break;
            default:
               MiniMessageParser.Style childStyle = applyStyleTag(el, style);

               for (MiniMessageParser.Node child : el.children) {
                  render(child, childStyle, out);
               }
         }
      }
   }

   private static void emit(String text, MiniMessageParser.Style style, List<BaseComponent> out) {
      if (!text.isEmpty()) {
         TextComponent c = new TextComponent(text);
         style.applyTo(c);
         out.add(c);
      }
   }

   private static void emitComponent(BaseComponent c, MiniMessageParser.Style style, List<BaseComponent> out) {
      style.applyTo(c);
      out.add(c);
   }

   private static MiniMessageParser.Style applyStyleTag(MiniMessageParser.ElementNode el, MiniMessageParser.Style parent) {
      MiniMessageParser.Style s = parent.copy();
      String name = el.name;
      if (NAMED_COLORS.containsKey(name)) {
         s.color = NAMED_COLORS.get(name);
         return s;
      } else if (isHex(name)) {
         s.color = ChatColor.of(name);
         return s;
      } else if (!name.equals("color") && !name.equals("colour") && !name.equals("c")) {
         if (name.startsWith("!") && DECORATIONS.containsKey(name.substring(1))) {
            setDecoration(s, DECORATIONS.get(name.substring(1)), false);
            return s;
         } else if (!DECORATIONS.containsKey(name)) {
            switch (name) {
               case "font":
                  if (!el.args.isEmpty()) {
                     s.font = joinFrom(el.args, 0);
                  }
                  break;
               case "insert":
               case "insertion":
                  if (!el.args.isEmpty()) {
                     s.insertion = joinFrom(el.args, 0);
                  }
                  break;
               case "hover":
                  s.hover = parseHover(el.args);
                  break;
               case "click":
                  if (el.args.size() >= 2) {
                     Action action = clickAction(el.args.get(0));
                     if (action != null) {
                        s.click = new ClickEvent(action, joinFrom(el.args, 1));
                     }
                  }
            }

            return s;
         } else {
            boolean value = el.args.isEmpty() || !el.args.get(0).equalsIgnoreCase("false");
            setDecoration(s, DECORATIONS.get(name), value);
            return s;
         }
      } else {
         if (!el.args.isEmpty()) {
            ChatColor cc = resolveColor(el.args.get(0));
            if (cc != null) {
               s.color = cc;
            }
         }

         return s;
      }
   }

   private static HoverEvent parseHover(List<String> args) {
      if (args.size() < 2) {
         return null;
      } else {
         String action = args.get(0).toLowerCase(Locale.ROOT);
         switch (action) {
            case "show_text":
               return new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text(parse(joinFrom(args, 1)))});
            case "show_item":
               List<String> rest = args.subList(1, args.size());
               int count = 1;
               int idEnd = rest.size();
               if (rest.size() >= 2 && isInteger(rest.get(rest.size() - 1))) {
                  count = Integer.parseInt(rest.get(rest.size() - 1).trim());
                  idEnd = rest.size() - 1;
               }

               String id = String.join(":", rest.subList(0, idEnd));
               return new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ITEM, new Content[]{new Item(id, count, null)});
            case "show_entity":
               List<String> rest = args.subList(1, args.size());
               int uuidIdx = -1;

               for (int k = 0; k < rest.size(); k++) {
                  if (isUuid(rest.get(k))) {
                     uuidIdx = k;
                     break;
                  }
               }

               if (uuidIdx < 0) {
                  return null;
               } else {
                  String type = String.join(":", rest.subList(0, uuidIdx));
                  String id = rest.get(uuidIdx);
                  BaseComponent name = uuidIdx + 1 < rest.size() ? new TextComponent(parse(joinFrom(rest, uuidIdx + 1))) : null;
                  return new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ENTITY, new Content[]{new Entity(type, id, name)});
               }
            default:
               return null;
         }
      }
   }

   private static String joinFrom(List<String> args, int from) {
      return from >= args.size() ? "" : String.join(":", args.subList(from, args.size()));
   }

   private static boolean isInteger(String s) {
      s = s.trim();
      if (s.isEmpty()) {
         return false;
      } else {
         for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean isUuid(String s) {
      return s.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
   }

   private static void setDecoration(MiniMessageParser.Style s, String canonical, boolean v) {
      switch (canonical) {
         case "bold":
            s.bold = v;
            break;
         case "italic":
            s.italic = v;
            break;
         case "underlined":
            s.underlined = v;
            break;
         case "strikethrough":
            s.strikethrough = v;
            break;
         case "obfuscated":
            s.obfuscated = v;
      }
   }

   private static ChatColor resolveColor(String token) {
      String t = token.toLowerCase(Locale.ROOT);
      if (NAMED_COLORS.containsKey(t)) {
         return NAMED_COLORS.get(t);
      } else {
         return isHex(t) ? ChatColor.of(t) : null;
      }
   }

   private static Action clickAction(String name) {
      try {
         return Action.valueOf(name.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   private static void renderGradient(MiniMessageParser.ElementNode el, List<Color> colors, MiniMessageParser.Style style, List<BaseComponent> out) {
      if (colors.size() >= 2) {
         int total = Math.max(1, countCodePoints(el));
         renderGradientChildren(el, colors, total, new int[]{0}, style, out);
      } else {
         for (MiniMessageParser.Node child : el.children) {
            render(child, style, out);
         }
      }
   }

   private static void renderGradientChildren(
      MiniMessageParser.ElementNode el, List<Color> colors, int total, int[] idx, MiniMessageParser.Style style, List<BaseComponent> out
   ) {
      for (MiniMessageParser.Node child : el.children) {
         if (child instanceof MiniMessageParser.TextNode tn) {
            String text = tn.text();

            for (int off = 0; off < text.length(); idx[0]++) {
               int cp = text.codePointAt(off);
               off += Character.charCount(cp);
               float t = total <= 1 ? 0.0F : (float)idx[0] / (total - 1);
               out.add(coloredCodePoint(cp, gradientColor(colors, t), style));
            }
         } else {
            MiniMessageParser.ElementNode child2 = (MiniMessageParser.ElementNode)child;
            if (child2.name.equals("newline") || child2.name.equals("br")) {
               emit("\n", style, out);
               idx[0]++;
            } else if (producesOwnColor(child2.name)) {
               render(child2, style, out);
               idx[0] += countCodePoints(child2);
            } else {
               renderGradientChildren(child2, colors, total, idx, applyStyleTag(child2, style), out);
            }
         }
      }
   }

   private static boolean producesOwnColor(String name) {
      return isColorTag(name)
         || name.equals("gradient")
         || name.equals("g")
         || name.equals("rainbow")
         || name.equals("r")
         || name.equals("transition")
         || name.equals("pride");
   }

   private static void renderTransition(MiniMessageParser.ElementNode el, List<Color> colors, MiniMessageParser.Style style, List<BaseComponent> out) {
      if (colors.isEmpty()) {
         for (MiniMessageParser.Node child : el.children) {
            render(child, style, out);
         }
      } else {
         float phase = lastPhase(el.args);
         Color color = colors.size() == 1 ? colors.get(0) : gradientColor(colors, (phase + 1.0F) / 2.0F);
         MiniMessageParser.Style s = style.copy();
         s.color = toChatColor(color);

         for (MiniMessageParser.Node child : el.children) {
            render(child, s, out);
         }
      }
   }

   private static void renderRainbow(MiniMessageParser.ElementNode el, MiniMessageParser.Style style, List<BaseComponent> out) {
      float phase = lastPhase(el.args);
      String text = flatten(el);
      int total = Math.max(1, text.codePointCount(0, text.length()));
      int idx = 0;

      for (int off = 0; off < text.length(); idx++) {
         int cp = text.codePointAt(off);
         off += Character.charCount(cp);
         float hue = ((float)idx / total + phase) % 1.0F;
         if (hue < 0.0F) {
            hue++;
         }

         out.add(coloredCodePoint(cp, Color.getHSBColor(hue, 1.0F, 1.0F), style));
      }
   }

   private static float lastPhase(List<String> args) {
      if (args.isEmpty()) {
         return 0.0F;
      } else {
         try {
            return Float.parseFloat(args.get(args.size() - 1).replace("!", ""));
         } catch (NumberFormatException var2) {
            return 0.0F;
         }
      }
   }

   private static BaseComponent coloredCodePoint(int cp, Color color, MiniMessageParser.Style style) {
      TextComponent c = new TextComponent(new String(Character.toChars(cp)));
      MiniMessageParser.Style s = style.copy();
      s.color = toChatColor(color);
      s.applyTo(c);
      return c;
   }

   private static ChatColor toChatColor(Color color) {
      return ChatColor.of(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
   }

   private static Color gradientColor(List<Color> colors, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      float scaled = t * (colors.size() - 1);
      int i1 = Math.min((int)scaled, colors.size() - 1);
      int i2 = Math.min(i1 + 1, colors.size() - 1);
      float local = scaled - i1;
      Color a = colors.get(i1);
      Color b = colors.get(i2);
      return new Color(
         Math.round(a.getRed() + local * (b.getRed() - a.getRed())),
         Math.round(a.getGreen() + local * (b.getGreen() - a.getGreen())),
         Math.round(a.getBlue() + local * (b.getBlue() - a.getBlue()))
      );
   }

   private static List<Color> parseColors(List<String> args) {
      List<Color> colors = new ArrayList<>();

      for (String a : args) {
         if (isHex(a)) {
            colors.add(hexToColor(a));
         } else {
            ChatColor cc = resolveColor(a);
            if (cc != null) {
               colors.add(cc.getColor());
            }
         }
      }

      return colors;
   }

   private static List<Color> prideColors(List<String> args) {
      String flag = args.isEmpty() ? "pride" : args.get(0).toLowerCase(Locale.ROOT);

      String[] hexes = switch (flag) {
         case "trans" -> new String[]{"#5BCEFA", "#F5A9B8", "#FFFFFF", "#F5A9B8", "#5BCEFA"};
         case "bi" -> new String[]{"#D60270", "#9B4F96", "#0038A8"};
         case "pan" -> new String[]{"#FF218C", "#FFD800", "#21B1FF"};
         case "lesbian" -> new String[]{"#D52D00", "#FF9A56", "#FFFFFF", "#D362A4", "#A30262"};
         case "nonbinary", "enby" -> new String[]{"#FCF434", "#FFFFFF", "#9C59D1", "#2C2C2C"};
         default -> new String[]{"#E40303", "#FF8C00", "#FFED00", "#008026", "#004DFF", "#750787"};
      };
      List<Color> colors = new ArrayList<>(hexes.length);

      for (String h : hexes) {
         colors.add(hexToColor(h));
      }

      return colors;
   }

   private static int countCodePoints(MiniMessageParser.Node node) {
      if (node instanceof MiniMessageParser.TextNode tn) {
         return tn.text().codePointCount(0, tn.text().length());
      } else {
         MiniMessageParser.ElementNode el = (MiniMessageParser.ElementNode)node;
         if (!el.name.equals("newline") && !el.name.equals("br")) {
            int sum = 0;

            for (MiniMessageParser.Node child : el.children) {
               sum += countCodePoints(child);
            }

            return sum;
         } else {
            return 1;
         }
      }
   }

   private static String flatten(MiniMessageParser.Node node) {
      StringBuilder sb = new StringBuilder();
      flatten(node, sb);
      return sb.toString();
   }

   private static void flatten(MiniMessageParser.Node node, StringBuilder sb) {
      if (node instanceof MiniMessageParser.TextNode tn) {
         sb.append(tn.text());
      } else if (node instanceof MiniMessageParser.ElementNode el) {
         if (el.name.equals("newline") || el.name.equals("br")) {
            sb.append('\n');
            return;
         }

         for (MiniMessageParser.Node child : el.children) {
            flatten(child, sb);
         }
      }
   }

   private static Color hexToColor(String hex) {
      return new Color(Integer.parseInt(hex.substring(1, 3), 16), Integer.parseInt(hex.substring(3, 5), 16), Integer.parseInt(hex.substring(5, 7), 16));
   }

   private record CloseTok(String name) implements MiniMessageParser.Token {
   }

   private static final class ElementNode implements MiniMessageParser.Node {
      final String name;
      final List<String> args;
      final List<MiniMessageParser.Node> children = new ArrayList<>();

      ElementNode(String name, List<String> args) {
         this.name = name;
         this.args = args;
      }
   }

   private sealed interface Node permits MiniMessageParser.TextNode, MiniMessageParser.ElementNode {
   }

   private record OpenTok(String name, List<String> args, boolean selfClose) implements MiniMessageParser.Token {
   }

   private static final class Style {
      ChatColor color;
      Boolean bold;
      Boolean italic;
      Boolean underlined;
      Boolean strikethrough;
      Boolean obfuscated;
      String font;
      String insertion;
      HoverEvent hover;
      ClickEvent click;

      MiniMessageParser.Style copy() {
         MiniMessageParser.Style s = new MiniMessageParser.Style();
         s.color = this.color;
         s.bold = this.bold;
         s.italic = this.italic;
         s.underlined = this.underlined;
         s.strikethrough = this.strikethrough;
         s.obfuscated = this.obfuscated;
         s.font = this.font;
         s.insertion = this.insertion;
         s.hover = this.hover;
         s.click = this.click;
         return s;
      }

      void applyTo(BaseComponent comp) {
         if (this.color != null) {
            comp.setColor(this.color);
         }

         if (this.bold != null) {
            comp.setBold(this.bold);
         }

         if (this.italic != null) {
            comp.setItalic(this.italic);
         }

         if (this.underlined != null) {
            comp.setUnderlined(this.underlined);
         }

         if (this.strikethrough != null) {
            comp.setStrikethrough(this.strikethrough);
         }

         if (this.obfuscated != null) {
            comp.setObfuscated(this.obfuscated);
         }

         if (this.font != null) {
            comp.setFont(this.font);
         }

         if (this.insertion != null) {
            comp.setInsertion(this.insertion);
         }

         if (this.hover != null) {
            comp.setHoverEvent(this.hover);
         }

         if (this.click != null) {
            comp.setClickEvent(this.click);
         }
      }
   }

   private record TextNode(String text) implements MiniMessageParser.Node {
   }

   private record TextTok(String text) implements MiniMessageParser.Token {
   }

   private sealed interface Token permits MiniMessageParser.TextTok, MiniMessageParser.OpenTok, MiniMessageParser.CloseTok {
   }
}
