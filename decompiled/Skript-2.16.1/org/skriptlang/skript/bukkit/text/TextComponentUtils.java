package org.skriptlang.skript.bukkit.text;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

public final class TextComponentUtils {
   private static final ConverterInfo<Object, Component> OBJECT_COMPONENT_CONVERTER = new ConverterInfo<>(
      Object.class, Component.class, TextComponentUtils::from, 0
   );
   private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("[&§]x(?:[&§][a-fA-F0-9]){6}");

   public static Component from(Object message) {
      return (Component)(switch (message) {
         case Component component -> component;
         case String string -> TextComponentParser.instance().parseSafe(string);
         default -> Component.text(Classes.toString(message));
      });
   }

   public static Component joinByNewLine(Component... components) {
      Component combined = components[0];

      for (int i = 1; i < components.length; i++) {
         combined = combined.appendNewline().append(components[i]);
      }

      return combined.compact();
   }

   public static Component appendToEnd(Component base, Component appendee) {
      return appendToLastChild(base, appendee).compact();
   }

   private static Component appendToLastChild(Component base, Component appendee) {
      List<Component> baseChildren = base.children();
      if (baseChildren.isEmpty()) {
         return base.append(appendee);
      } else {
         List<Component> var3 = new ArrayList<>(baseChildren);
         var3.addLast(appendToLastChild((Component)var3.removeLast(), appendee));
         return base.children(var3);
      }
   }

   public static String replaceLegacyFormattingCodes(String text) {
      if (!text.contains("&") && !text.contains("§")) {
         return text;
      } else {
         text = LEGACY_HEX_PATTERN.matcher(text).replaceAll(result -> {
            String hex = result.group();
            StringBuilder replacement = new StringBuilder();
            replacement.append("<#");

            for (int i = 3; i <= 13; i += 2) {
               replacement.append(hex.charAt(i));
            }

            replacement.append('>');
            return Matcher.quoteReplacement(replacement.toString());
         });
         return TextComponentParser.LEGACY_CODE_PATTERN.matcher(text).replaceAll(result -> {
            String backslashes = result.group(1);
            if (backslashes.length() % 2 == 1) {
               return Matcher.quoteReplacement(result.group().substring(1));
            } else {
               if (!backslashes.isEmpty()) {
                  backslashes = backslashes.substring(1);
               }

               StringBuilder replacement = new StringBuilder(backslashes);
               ChatColor color = ChatColor.getByChar(Character.toLowerCase(result.group(2).charAt(1)));

               assert color != null;

               replacement.append('<').append(color.asBungee().getName()).append('>');
               return Matcher.quoteReplacement(replacement.toString());
            }
         });
      }
   }

   @Nullable
   public static Expression<? extends Component> asComponentExpression(Expression<?> expression) {
      expression = LiteralUtils.defendExpression(expression);
      if (!LiteralUtils.canInitSafely(expression)) {
         return null;
      } else {
         boolean canReturnComponent = Arrays.stream(expression.possibleReturnTypes())
            .allMatch(type -> type != Object.class && Converters.converterExists((Class<?>)type, Component.class));
         if (canReturnComponent) {
            Expression<? extends Component> componentExpression = expression.getConvertedExpression(Component.class);
            if (componentExpression != null) {
               return componentExpression;
            }
         }

         return new ConvertedExpression<>(expression, Component.class, OBJECT_COMPONENT_CONVERTER);
      }
   }

   private TextComponentUtils() {
   }
}
