package org.skriptlang.skript.bukkit.text.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Raw String")
@Description({"Returns the string without formatting (colors, decorations, etc.) and without stripping them from it.", "For example, <code>raw \"&aHello There!\"</code> would output <code>&aHello There!</code>"})
@Example("send raw \"&aThis text is unformatted!\" to all players")
@Since({"2.7"})
public class ExprRawString extends SimplePropertyExpression<String, Object> {
   private boolean isComponent = true;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                     ExprRawString.class, Object.class
                  )
                  .supplier(ExprRawString::new))
               .addPatterns(new String[]{"raw %strings%"}))
            .build()
      );
   }

   public Object convert(String from) {
      return this.isComponent ? Component.text(from) : from;
   }

   @Override
   public Class<?> getReturnType() {
      return this.isComponent ? Component.class : String.class;
   }

   @Override
   protected String getPropertyName() {
      return "raw";
   }

   @SafeVarargs
   @Nullable
   @Override
   public final <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
      for (Class<R> clazz : to) {
         if (String.class.isAssignableFrom(clazz)) {
            ExprRawString converted = new ExprRawString();
            converted.setExpr(this.getExpr());
            converted.rawExpr = this.rawExpr;
            converted.isComponent = false;
            return converted;
         }
      }

      return super.getConvertedExpression(to);
   }
}
