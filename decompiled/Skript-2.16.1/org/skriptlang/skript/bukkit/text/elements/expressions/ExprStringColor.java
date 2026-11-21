package org.skriptlang.skript.bukkit.text.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("String Colors")
@Description({"Retrieve the first, the last, or all of the color objects or color codes of a string.", "The retrieved color codes of the string will be formatted with the color symbol."})
@Example.Examples({@Example("set {_colors::*} to the string colors of \"<red>hey<blue>yo\""), @Example("set {_color} to the first string color code of \"&aGoodbye!\"\nsend \"%{_color}%Howdy!\" to all players\n")})
@Since({"2.11"})
public class ExprStringColor extends PropertyExpression<String, Object> {
   private ExprStringColor.StringColor selectedState;
   private boolean getCodes;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprStringColor.class, Object.class
                     )
                     .supplier(ExprStringColor::new))
                  .priority(DEFAULT_PRIORITY))
               .addPatterns(
                  new String[]{
                     "[all [[of] the]|the] string colo[u]r[s] [code:code[s]] of %strings%",
                     "[the] first string colo[u]r[s] [code:code[s]] of %strings%",
                     "[the] last string colo[u]r[s] [code:code[s]] of %strings%"
                  }
               ))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.selectedState = ExprStringColor.StringColor.values()[matchedPattern];
      this.getCodes = parseResult.hasTag("code");
      this.setExpr((Expression<? extends String>)exprs[0]);
      return true;
   }

   protected Object[] get(Event event, String[] source) {
      List<Object> colors = new ArrayList<>();

      for (String string : this.getExpr().getArray(event)) {
         colors.addAll(this.getColors(string));
      }

      return colors.toArray((Object[])Array.newInstance(this.getReturnType(), 0));
   }

   @Override
   public Class<?> getReturnType() {
      return this.getCodes ? String.class : Color.class;
   }

   @Override
   public Class<?>[] possibleReturnTypes() {
      return this.getCodes ? CollectionUtils.array(String.class) : CollectionUtils.array(SkriptColor.class, ColorRGB.class);
   }

   @Override
   public boolean isSingle() {
      return this.selectedState != ExprStringColor.StringColor.ALL && this.getExpr().isSingle();
   }

   @Override
   public Expression<?> simplify() {
      return (Expression<?>)(this.getExpr() instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

      builder.append(switch (this.selectedState) {
         case ALL -> "all of the";
         case FIRST -> "the first";
         case LAST -> "the last";
      });
      if (this.getCodes) {
         builder.append("string color codes");
      } else {
         builder.append("string colors");
      }

      builder.append("of", this.getExpr());
      return builder.toString();
   }

   private List<Object> getColors(String string) {
      List<Object> colors = new ArrayList<>();
      int length = string.length();
      TextComponentParser textComponentParser = TextComponentParser.instance();

      for (int index = 0; index < length; index++) {
         boolean isTag = string.charAt(index) == '<';
         if (isTag || string.charAt(index) == '&' || string.charAt(index) == 167) {
            if (index + 1 == length) {
               break;
            }

            if (isTag) {
               int end = string.indexOf(62, index + 1);
               if (end != -1) {
                  String tag = string.substring(index + 1, end);
                  if (!tag.isEmpty()) {
                     if (tag.charAt(0) == '#') {
                        tag = tag.substring(1);
                        int tagLength = tag.length();
                        if (tagLength == 6 || tagLength == 8) {
                           colors.add(this.getCodes ? "<#" + tag + ">" : this.fromHex(tag));
                           if (this.selectedState == ExprStringColor.StringColor.FIRST) {
                              break;
                           }

                           index += tagLength + 2;
                        }
                     } else {
                        String enclosedTag = "<" + tag + ">skript";
                        String parsed = textComponentParser.toLegacyString(textComponentParser.parseSafe(enclosedTag));
                        if (!parsed.equals(enclosedTag)) {
                           SkriptColor color = SkriptColor.fromColorChar(parsed.charAt(1));
                           if (color != null) {
                              colors.add(this.getCodes ? "<" + tag + ">" : color);
                              if (this.selectedState == ExprStringColor.StringColor.FIRST) {
                                 break;
                              }

                              index += tag.length() + 1;
                           }
                        }
                     }
                  }
               }
            } else {
               boolean checkHex = this.checkHex(string, index);
               SkriptColor checkChar = SkriptColor.fromColorChar(string.charAt(index + 1));
               if (checkHex) {
                  String hexString = string.substring(index, index + 14);
                  colors.add(this.getCodes ? hexString : this.fromHex(hexString));
                  if (this.selectedState == ExprStringColor.StringColor.FIRST) {
                     break;
                  }

                  index += 13;
               } else if (checkChar != null) {
                  String colorString = string.substring(index, index + 2);
                  colors.add(this.getCodes ? colorString : checkChar);
                  if (this.selectedState == ExprStringColor.StringColor.FIRST) {
                     break;
                  }

                  index++;
               }
            }
         }
      }

      if (this.selectedState == ExprStringColor.StringColor.LAST && !colors.isEmpty()) {
         Object last = colors.getLast();
         colors.clear();
         colors.add(last);
      }

      return colors;
   }

   private boolean checkHex(String string, int index) {
      int length = string.length();
      if (length < index + 12) {
         return false;
      } else if (string.charAt(index + 1) != 'x') {
         return false;
      } else {
         for (int i = index + 2; i <= index; i += 2) {
            if (string.charAt(i) != '&' && string.charAt(i) != 167) {
               return false;
            }
         }

         for (int ix = index + 3; ix <= index; ix += 2) {
            char toCheck = string.charAt(ix);
            if (toCheck < '0' || toCheck > 'f') {
               return false;
            }

            if (toCheck > '9' && toCheck < 'A') {
               return false;
            }

            if (toCheck > 'F' && toCheck < 'a') {
               return false;
            }
         }

         return true;
      }
   }

   private ColorRGB fromHex(@NotNull String hex) {
      if (hex.startsWith("&x") || hex.startsWith("§x")) {
         hex = hex.substring(2);
      }

      hex = hex.replaceAll("[§&]", "");
      int length = hex.length();
      int alpha = 255;
      int red;
      int green;
      int blue;
      if (length == 6) {
         red = Integer.parseInt(hex.substring(0, 2), 16);
         green = Integer.parseInt(hex.substring(2, 4), 16);
         blue = Integer.parseInt(hex.substring(4, 6), 16);
      } else {
         if (length != 8) {
            throw new UnsupportedOperationException("Unsupported hex format - requires #RRGGBB or #AARRGGBB");
         }

         alpha = Integer.parseInt(hex.substring(0, 2), 16);
         red = Integer.parseInt(hex.substring(2, 4), 16);
         green = Integer.parseInt(hex.substring(4, 6), 16);
         blue = Integer.parseInt(hex.substring(6, 8), 16);
      }

      return ColorRGB.fromRGBA(red, green, blue, alpha);
   }

   private static enum StringColor {
      ALL,
      FIRST,
      LAST;
   }
}
