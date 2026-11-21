package ac.grim.grimac.utils.data.webhook.discord;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record CompiledDiscordTemplate(CompiledDiscordTemplate.Segment[] segments) {
   private static final Pattern PLACEHOLDER = Pattern.compile("%([a-zA-Z0-9_]+)%");

   public static CompiledDiscordTemplate compile(String template) {
      List<CompiledDiscordTemplate.Segment> parts = new ArrayList<>();
      Matcher m = PLACEHOLDER.matcher(template);
      CompiledDiscordTemplate.MarkdownContext ctx = CompiledDiscordTemplate.MarkdownContext.NORMAL;

      int lastEnd;
      for (lastEnd = 0; m.find(); lastEnd = m.end()) {
         String gap = template.substring(lastEnd, m.start());
         if (!gap.isEmpty()) {
            parts.add(new CompiledDiscordTemplate.Literal(gap));
         }

         ctx = advanceContext(ctx, gap);

         CompiledDiscordTemplate.EscapeMode mode = switch (ctx) {
            case NORMAL -> CompiledDiscordTemplate.EscapeMode.FULL_MARKDOWN;
            case INLINE_CODE, CODE_BLOCK -> CompiledDiscordTemplate.EscapeMode.CODE_SPAN;
         };
         parts.add(new CompiledDiscordTemplate.Placeholder(m.group(0), mode));
      }

      if (lastEnd < template.length()) {
         parts.add(new CompiledDiscordTemplate.Literal(template.substring(lastEnd)));
      }

      return new CompiledDiscordTemplate(parts.toArray(CompiledDiscordTemplate.Segment[]::new));
   }

   public String render(
      @NotNull GrimPlayer player, @NotNull Map<String, String> statics, @NotNull Map<String, Function<GrimUser, String>> dynamics, char backtickReplacement
   ) {
      StringBuilder sb = new StringBuilder(this.segments.length * 32);

      for (CompiledDiscordTemplate.Segment seg : this.segments) {
         if (seg instanceof CompiledDiscordTemplate.Literal l) {
            sb.append(l.text);
         } else if (seg instanceof CompiledDiscordTemplate.Placeholder p) {
            String val = statics.get(p.key);
            if (val == null) {
               Function<GrimUser, String> fn = dynamics.get(p.key);
               if (fn != null) {
                  val = fn.apply(player);
               }
            }

            if (val == null) {
               String resolved = GrimAPI.INSTANCE.getMessagePlaceHolderManager().replacePlaceholders(player.platformPlayer, p.key);
               if (!resolved.equals(p.key)) {
                  val = resolved;
               }
            }

            if (val != null) {
               sb.append(escape(val, p.mode, backtickReplacement));
            } else {
               sb.append(p.key);
            }
         }
      }

      return sb.toString();
   }

   private static String escape(String value, CompiledDiscordTemplate.EscapeMode mode, char backtickReplacement) {
      if (mode == CompiledDiscordTemplate.EscapeMode.NONE) {
         return value;
      } else {
         return mode == CompiledDiscordTemplate.EscapeMode.CODE_SPAN ? escapeCodeSpan(value, backtickReplacement) : escapeMarkdown(value);
      }
   }

   public static String escapeMarkdown(String s) {
      if (s != null && !s.isEmpty()) {
         StringBuilder sb = new StringBuilder(s.length() + 16);

         for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
               case '\n':
                  sb.append("\\n");
                  break;
               case '#':
                  sb.append("\\#");
                  break;
               case '(':
                  sb.append("\\(");
                  break;
               case ')':
                  sb.append("\\)");
                  break;
               case '*':
                  sb.append("\\*");
                  break;
               case '-':
                  sb.append("\\-");
                  break;
               case '.':
                  sb.append("\\.");
                  break;
               case ':':
                  sb.append("\\:");
                  break;
               case '<':
                  sb.append("\\<");
                  break;
               case '>':
                  sb.append("\\>");
                  break;
               case '[':
                  sb.append("\\[");
                  break;
               case '\\':
                  sb.append("\\\\");
                  break;
               case ']':
                  sb.append("\\]");
                  break;
               case '_':
                  sb.append("\\_");
                  break;
               case '`':
                  sb.append("\\`");
                  break;
               case '|':
                  sb.append("\\|");
                  break;
               case '~':
                  sb.append("\\~");
                  break;
               default:
                  sb.append(c);
            }
         }

         return sb.toString();
      } else {
         return s;
      }
   }

   public static String escapeCodeSpan(String s, char replacement) {
      return s != null && !s.isEmpty() && replacement != '`' ? s.replace('`', replacement) : s;
   }

   private static CompiledDiscordTemplate.MarkdownContext advanceContext(CompiledDiscordTemplate.MarkdownContext ctx, String text) {
      int i = 0;

      while (i < text.length()) {
         char c = text.charAt(i);
         if (ctx == CompiledDiscordTemplate.MarkdownContext.NORMAL) {
            if (c == '\\' && i + 1 < text.length()) {
               i += 2;
               continue;
            }

            if (c == '`') {
               if (i + 2 < text.length() && text.charAt(i + 1) == '`' && text.charAt(i + 2) == '`') {
                  ctx = CompiledDiscordTemplate.MarkdownContext.CODE_BLOCK;
                  i += 3;
                  continue;
               }

               ctx = CompiledDiscordTemplate.MarkdownContext.INLINE_CODE;
            }
         } else if (ctx == CompiledDiscordTemplate.MarkdownContext.INLINE_CODE) {
            if (c == '`') {
               ctx = CompiledDiscordTemplate.MarkdownContext.NORMAL;
            }
         } else if (c == '`' && i + 2 < text.length() && text.charAt(i + 1) == '`' && text.charAt(i + 2) == '`') {
            ctx = CompiledDiscordTemplate.MarkdownContext.NORMAL;
            i += 3;
            continue;
         }

         i++;
      }

      return ctx;
   }

   public static enum EscapeMode {
      FULL_MARKDOWN,
      CODE_SPAN,
      NONE;
   }

   private record Literal(String text) implements CompiledDiscordTemplate.Segment {
   }

   private static enum MarkdownContext {
      NORMAL,
      INLINE_CODE,
      CODE_BLOCK;
   }

   private record Placeholder(String key, CompiledDiscordTemplate.EscapeMode mode) implements CompiledDiscordTemplate.Segment {
   }

   private sealed interface Segment permits CompiledDiscordTemplate.Literal, CompiledDiscordTemplate.Placeholder {
   }
}
