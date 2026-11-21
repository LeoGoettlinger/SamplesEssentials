package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operation;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.converter.Converters;

@Name("Metadata")
@Description({"Metadata is a way to store temporary data on entities, blocks and more that disappears after a server restart."})
@Example.Examples({@Example("set metadata value \"healer\" of player to true"), @Example("broadcast \"%metadata value \"healer\" of player%\""), @Example("clear metadata value \"healer\" of player")})
@Since({"2.2-dev36, 2.10 (add, remove)"})
public class ExprMetadata<T> extends SimpleExpression<T> {
   private final ExprMetadata<?> source;
   private final Class<? extends T>[] types;
   private final Class<T> superType;
   @UnknownNullability
   private Expression<String> keys;
   @UnknownNullability
   private Expression<Metadatable> holders;

   public ExprMetadata() {
      this(null, Object.class);
   }

   @SafeVarargs
   private ExprMetadata(ExprMetadata<?> source, Class<? extends T>... types) {
      this.source = source;
      if (source != null) {
         this.keys = source.keys;
         this.holders = source.holders;
      }

      this.types = types;
      this.superType = (Class<T>)Utils.getSuperType(types);
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.holders = (Expression<Metadatable>)exprs[matchedPattern ^ 1];
      this.keys = (Expression<String>)exprs[matchedPattern];
      return true;
   }

   @Nullable
   @Override
   protected T[] get(Event event) {
      List<Object> values = new ArrayList<>();
      String[] keys = this.keys.getArray(event);

      for (Metadatable holder : this.holders.getArray(event)) {
         for (String key : keys) {
            List<MetadataValue> metadata = holder.getMetadata(key);
            if (!metadata.isEmpty()) {
               values.add(metadata.get(metadata.size() - 1).value());
            }
         }
      }

      try {
         return Converters.convert(values.toArray(), this.types, this.superType);
      } catch (ClassCastException var13) {
         return (T[])((Object[])Array.newInstance(this.superType, 0));
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, ADD, REMOVE, DELETE -> (Class[])CollectionUtils.array(Object.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      String[] keys = this.keys.getArray(event);

      for (Metadatable holder : this.holders.getArray(event)) {
         for (String key : keys) {
            switch (mode) {
               case SET:
                  holder.setMetadata(key, new FixedMetadataValue(Skript.getInstance(), delta[0]));
                  break;
               case ADD:
               case REMOVE:
                  assert delta != null;

                  Operator operator = mode == Changer.ChangeMode.ADD ? Operator.ADDITION : Operator.SUBTRACTION;
                  List<MetadataValue> metadata = holder.getMetadata(key);
                  Object value = metadata.isEmpty() ? null : metadata.get(metadata.size() - 1).value();
                  OperationInfo<?, ?, ?> info;
                  if (value != null) {
                     info = Arithmetics.getOperationInfo(operator, value.getClass(), delta[0].getClass());
                     if (info == null) {
                        break;
                     }
                  } else {
                     info = Arithmetics.getOperationInfo(operator, delta[0].getClass(), delta[0].getClass());
                     if (info == null) {
                        break;
                     }

                     value = Arithmetics.getDefaultValue(info.left());
                     if (value == null) {
                        break;
                     }
                  }

                  Object newValue = ((Operation<?, Object, ?>)info.operation()).calculate(value, delta[0]);
                  holder.setMetadata(key, new FixedMetadataValue(Skript.getInstance(), newValue));
                  break;
               case DELETE:
                  holder.removeMetadata(key, Skript.getInstance());
            }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return this.holders.isSingle() && this.keys.isSingle();
   }

   @Override
   public Class<? extends T> getReturnType() {
      return this.superType;
   }

   @Override
   public Class<? extends T>[] possibleReturnTypes() {
      return Arrays.copyOf(this.types, this.types.length);
   }

   @SafeVarargs
   @Override
   public final <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
      return new ExprMetadata<>(this, to);
   }

   @Override
   public Expression<?> getSource() {
      return this.source == null ? this : this.source;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "metadata values " + this.keys.toString(event, debug) + " of " + this.holders.toString(event, debug);
   }

   static {
      Skript.registerExpression(
         ExprMetadata.class,
         Object.class,
         ExpressionType.PROPERTY,
         "metadata [(value|tag)[s]] %strings% of %metadataholders%",
         "%metadataholders%'[s] metadata [(value|tag)[s]] %string%"
      );
   }
}
