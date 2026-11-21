package org.skriptlang.skript.common.elements.expressions;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Text Replacement")
@Description({"Performs a text replacement on a given value, returning the result. Supports regex and case sensitive replacement."})
@Example.Examples({@Example("send \"Welcome [player]\" where \"[player]\" is replaced with \"%player%\" to player"), @Example("# Function for sanitizing user inputs\n# Strips the input of any non-alphanumeric characters using regex\nfunction sanitizeInput(input: string) :: string:\n\treturn {_input} where regex pattern \"\\W\" is replaced with \"\"\n"), @Example("# Function to convert &# hex color codes to <# > (mini message format)\nfunction colorFormat(input: string) :: string:\n\treturn {_input} where all instances of regex pattern \"&#([a-fA-F0-9]{6})\" are replaced with \"<#$1>\"\n"), @Example("# Very simple chat censor\non chat:\n\tset message to message where all instances of \"idiot\", \"noob\" are replaced with \"****\"\n\tset message to message where regex \"\\b(idiot|noob)\\b\" is replaced with \"****\" # Regex version using word boundaries for better results\n")})
@Since({"2.16"})
public class ExprReplace extends SimpleExpression<String> {
   private Expression<String> needleExpr;
   private Expression<String> haystackExpr;
   private Expression<String> replacementExpr;
   private boolean isFirst;
   private boolean isRegex;
   private boolean isCaseSensitive;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(ExprReplace.class, String.class)
                  .addPatterns(
                     new String[]{
                        "%strings% where [(first:[the] first instance[s]|all instances) of] %strings% (is|are) replaced with %string% [regex:using regex|case:with case sensitivity]",
                        "%strings% where [(first:[the] first instance[s]|all instances) of] regex [pattern[s]] %strings% (is|are) replaced with %string%"
                     }
                  ))
               .supplier(ExprReplace::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expr, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.haystackExpr = (Expression<String>)expr[0];
      this.needleExpr = (Expression<String>)expr[1];
      this.replacementExpr = (Expression<String>)expr[2];
      this.isRegex = matchedPattern == 1 || parseResult.hasTag("regex");
      this.isFirst = parseResult.hasTag("first");
      this.isCaseSensitive = SkriptConfig.caseSensitive.value() || parseResult.hasTag("case");
      return true;
   }

   @Nullable
   protected String[] get(Event event) {
      String replacement = this.replacementExpr.getSingle(event);
      String[] needles = this.needleExpr.getArray(event);
      String[] haystacks = this.haystackExpr.getArray(event);
      if (replacement == null) {
         return haystacks;
      } else {
         List<String> result = new ArrayList<>(haystacks.length);
         if (this.isRegex) {
            List<Pattern> patterns = new ArrayList<>(needles.length);

            for (String needle : needles) {
               try {
                  patterns.add(Pattern.compile(needle));
               } catch (Exception var16) {
               }
            }

            for (String haystack : haystacks) {
               for (Pattern pattern : patterns) {
                  Matcher matcher = pattern.matcher(haystack);

                  try {
                     if (this.isFirst) {
                        haystack = matcher.replaceFirst(replacement);
                     } else {
                        haystack = matcher.replaceAll(replacement);
                     }
                  } catch (IndexOutOfBoundsException var15) {
                  }
               }

               result.add(haystack);
            }
         } else {
            for (String haystack : haystacks) {
               for (String needle : needles) {
                  if (this.isFirst) {
                     haystack = StringUtils.replaceFirst(haystack, needle, replacement, this.isCaseSensitive);
                  } else {
                     haystack = StringUtils.replace(haystack, needle, replacement, this.isCaseSensitive);
                  }
               }

               result.add(haystack);
            }
         }

         return result.toArray(new String[0]);
      }
   }

   @Override
   public boolean isSingle() {
      return this.haystackExpr.isSingle();
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug)
         .append("replace")
         .appendIf(this.isFirst, "first")
         .append(this.needleExpr, "in", this.haystackExpr, "with", this.replacementExpr)
         .appendIf(this.isRegex, "using regex")
         .appendIf(this.isCaseSensitive, "with case sensitivity")
         .toString();
   }
}
