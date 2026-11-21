package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.common.AnyValued;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Value")
@Description({"Returns the value of something that has a value, e.g. a node in a config.", "The value is automatically converted to the specified type (e.g. text, number) where possible."})
@Example.Examples({@Example("set {_node} to node \"language\" in the skript config\nbroadcast the text value of {_node}\n"), @Example("set {_node} to node \"update check interval\" in the skript config\n\nbroadcast text value of {_node}\n# text value of {_node} = \"12 hours\" (text)\n\nwait for {_node}'s timespan value\n# timespan value of {_node} = 12 hours (duration)\n")})
@Since({"2.10 (Nodes), 2.10 (Any)"})
@Deprecated(
   since = "2.13",
   forRemoval = true
)
public class ExprValue extends SimplePropertyExpression<Object, Object> {
   private boolean isSingle;
   private ClassInfo<?> classInfo;

   @Override
   public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      Literal<ClassInfo<?>> format;
      switch (pattern) {
         case 0:
            this.isSingle = true;
         case 1:
            format = (Literal<ClassInfo<?>>)expressions[0];
            this.setExpr((Expression<? extends Object>)expressions[1]);
            break;
         case 2:
            this.isSingle = true;
         default:
            format = (Literal<ClassInfo<?>>)expressions[1];
            this.setExpr((Expression<? extends Object>)expressions[0]);
      }

      this.classInfo = format.getSingle();
      return true;
   }

   @Nullable
   @Override
   public Object convert(@Nullable Object object) {
      if (object == null) {
         return null;
      } else {
         return object instanceof AnyValued<?> valued ? valued.convertedValue(this.classInfo) : null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET -> new Class[]{Object.class};
         case RESET, DELETE -> new Class[0];
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Object newValue = delta != null ? delta[0] : null;

      for (Object object : this.getExpr().getArray(event)) {
         if (object instanceof AnyValued<?> valued && valued.supportsValueChange()) {
            valued.changeValueSafely(newValue);
         }
      }
   }

   @Override
   public Class<?> getReturnType() {
      return this.classInfo.getC();
   }

   @Override
   public boolean isSingle() {
      return this.isSingle;
   }

   @Override
   protected String getPropertyName() {
      return this.classInfo.getCodeName() + " value" + (this.isSingle ? "" : "s");
   }

   static {
      if (!SkriptConfig.useTypeProperties.value()) {
         Skript.registerExpression(
            ExprValue.class,
            Object.class,
            ExpressionType.PROPERTY,
            "[the] %*classinfo% value of %valued%",
            "[the] %*classinfo% values of %valueds%",
            "%valued%'s %*classinfo% value",
            "%valueds%'[s] %*classinfo% values"
         );
      }
   }
}
