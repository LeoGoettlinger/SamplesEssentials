package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.util.Kleenean;
import java.util.Iterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Value Within")
@Description({"Gets the value within objects. Usually used with variables to get the value they store rather than the variable itself, or with lists to get the values of a type."})
@Example.Examples({@Example("set {_entity} to a random entity out of all entities\ndelete entity within {_entity} # This deletes the entity itself and not the value stored in the variable\n"), @Example("set {_list::*} to \"something\", 10, \"test\" and a zombie\nbroadcast the strings within {_list::*} # \"something\", \"test\"\n")})
@Since({"2.7"})
public class ExprValueWithin extends WrapperExpression<Object> implements KeyProviderExpression<Object> {
   @Nullable
   private ClassInfo<?> classInfo;
   @Nullable
   private Changer changer;
   private boolean returnsKeys;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      boolean plural;
      if (exprs[0] != null) {
         Literal<ClassInfoReference> classInfoReference = (Literal<ClassInfoReference>)ClassInfoReference.wrap((Expression<ClassInfo<?>>)exprs[0]);
         plural = classInfoReference.getSingle().isPlural().isTrue();
      } else {
         plural = parseResult.hasTag("s");
      }

      if (plural == exprs[1].isSingle()) {
         if (plural) {
            Skript.error("You cannot get multiple elements of a single value");
         } else {
            Skript.error(exprs[1].toString(null, false) + " may contain more than one " + (this.classInfo == null ? "value" : this.classInfo.getName()));
         }

         return false;
      } else {
         this.classInfo = exprs[0] == null ? null : (ClassInfo)((Literal)exprs[0]).getSingle();
         Expression<?> expr = this.classInfo == null ? exprs[1] : exprs[1].getConvertedExpression(this.classInfo.getC());
         if (expr == null) {
            return false;
         } else {
            this.setExpr((Expression<? extends Object>)expr);
            this.returnsKeys = KeyProviderExpression.canReturnKeys(expr);
            return true;
         }
      }
   }

   @NotNull
   @Override
   public String[] getArrayKeys(Event event) throws IllegalStateException {
      if (!this.returnsKeys) {
         throw new IllegalStateException();
      } else {
         return ((KeyProviderExpression)this.getExpr()).getArrayKeys(event);
      }
   }

   @NotNull
   @Override
   public String[] getAllKeys(Event event) {
      if (!this.returnsKeys) {
         throw new IllegalStateException();
      } else {
         return ((KeyProviderExpression)this.getExpr()).getAllKeys(event);
      }
   }

   @Override
   public Iterator<KeyedValue<Object>> keyedIterator(Event event) {
      if (!this.returnsKeys) {
         throw new IllegalStateException();
      } else {
         return ((KeyProviderExpression)this.getExpr()).keyedIterator(event);
      }
   }

   @Override
   public boolean canReturnKeys() {
      return this.returnsKeys;
   }

   @Override
   public boolean areKeysRecommended() {
      return KeyProviderExpression.areKeysRecommended(this.getExpr());
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      this.changer = Classes.getSuperClassInfo(this.getReturnType()).getChanger();
      return this.changer == null ? null : this.changer.acceptChange(mode);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (this.changer == null) {
         throw new UnsupportedOperationException();
      } else {
         this.changer.change(this.getArray(event), delta, mode);
      }
   }

   @Override
   public boolean isIndexLoop(String input) {
      if (!this.returnsKeys) {
         throw new IllegalStateException();
      } else {
         return ((KeyProviderExpression)this.getExpr()).isIndexLoop(input);
      }
   }

   @Override
   public boolean isLoopOf(String input) {
      return this.getExpr().isLoopOf(input);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return (this.classInfo == null ? "value" : this.classInfo.toString(event, debug)) + " within " + this.getExpr();
   }

   static {
      Skript.registerExpression(ExprValueWithin.class, Object.class, ExpressionType.PROPERTY, "[the] (%-*classinfo%|value[:s]) (within|in) %~objects%");
   }
}
