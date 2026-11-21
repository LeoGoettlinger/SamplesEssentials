package org.skriptlang.skript.lang.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Experimental
public abstract class PropertyBaseExpression<Handler extends ExpressionPropertyHandler<?, ?>>
   extends SimpleExpression<Object>
   implements PropertyBaseSyntax<Handler> {
   protected Expression<?> expr;
   protected PropertyMap<Handler> properties;
   protected Class<?>[] returnTypes;
   protected Class<?> returnType;
   protected final Property<Handler> property = this.getProperty();
   protected boolean useCIP;
   private final PropertyBaseExpression<Handler>.ChangeDetails changeDetails = new PropertyBaseExpression.ChangeDetails();

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (!LiteralUtils.canInitSafely(LiteralUtils.defendExpression(expressions[0]))) {
         return false;
      } else {
         this.expr = PropertyBaseSyntax.asProperty(this.property, expressions[0]);
         if (this.expr == null) {
            Skript.error(this.getBadTypesErrorMessage(expressions[0]));
            return false;
         } else {
            this.properties = PropertyBaseSyntax.getPossiblePropertyInfos(this.property, this.expr);
            if (this.properties.isEmpty()) {
               Skript.error(this.getBadTypesErrorMessage(this.expr));
               return false;
            } else {
               for (Property.PropertyInfo<Handler> propertyInfo : this.properties.values()) {
                  if (propertyInfo.handler().requiresSourceExprChange()) {
                     this.useCIP = true;
                     break;
                  }
               }

               this.returnTypes = this.getPropertyReturnTypes(this.properties, ExpressionPropertyHandler::possibleReturnTypes);
               this.returnType = Utils.getSuperType(this.returnTypes);
               return LiteralUtils.canInitSafely(this.expr);
            }
         }
      }
   }

   @NotNull
   protected Class<?>[] getPropertyReturnTypes(@NotNull PropertyMap<Handler> properties, Function<Handler, Class<?>[]> getReturnType) {
      return properties.values()
         .stream()
         .flatMap(propertyInfo -> Arrays.stream(getReturnType.apply(propertyInfo.handler())))
         .filter(type -> type != Object.class)
         .toArray(Class[]::new);
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      return this.expr.stream(event).flatMap(source -> {
         Handler handler = this.properties.getHandler(source.getClass());
         if (handler == null) {
            return null;
         } else {
            Object value = this.convert(event, handler, source);
            return value != null && value.getClass().isArray() ? Arrays.stream((Object[])value) : Stream.of(value);
         }
      }).filter(Objects::nonNull).toArray(size -> (Object[])Array.newInstance(this.getReturnType(), size));
   }

   @Nullable
   protected <T> Object convert(Event event, Handler handler, T source) {
      return ((ExpressionPropertyHandler<T, ?>)handler).convert(event, source);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      Set<Class<?>> changableTypes = Set.of();
      if (this.useCIP) {
         changableTypes = this.properties
            .keySet()
            .stream()
            .filter(type -> Changer.ChangerUtils.acceptsChange(this.expr, Changer.ChangeMode.SET, type))
            .collect(Collectors.toSet());
         if (changableTypes.isEmpty()) {
            return null;
         }
      }

      Set<Class<?>> allowedChangeTypes = new HashSet<>();

      for (Entry<Class<?>, Property.PropertyInfo<Handler>> entry : this.properties.entrySet()) {
         Class<?> propertyType = entry.getKey();
         Property.PropertyInfo<Handler> propertyInfo = entry.getValue();
         if (this.useCIP && !changableTypes.contains(propertyType)) {
            this.changeDetails.storeTypes(mode, propertyInfo, null);
         }

         Class<?>[] types = propertyInfo.handler().acceptChange(mode);
         this.changeDetails.storeTypes(mode, propertyInfo, types);
         if (types != null) {
            if (mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.RESET) {
               return new Class[0];
            }

            allowedChangeTypes.addAll(Arrays.asList(types));
         }
      }

      return allowedChangeTypes.isEmpty() ? null : allowedChangeTypes.toArray(new Class[0]);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Function<Object, ?> updateTypeFunction = propertyHaver -> {
         Property.PropertyInfo<Handler> propertyInfo = this.properties.get(propertyHaver.getClass());
         if (propertyInfo == null) {
            return null;
         } else {
            Class<?>[] allowedTypes = this.changeDetails.getTypes(mode, propertyInfo);
            if (allowedTypes == null) {
               return null;
            } else if (mode != Changer.ChangeMode.DELETE && mode != Changer.ChangeMode.RESET) {
               assert delta != null;

               Object[] verifiedDelta = delta;
               Class<?>[] flatAllowedTypes = new Class[allowedTypes.length];

               for (int i = 0; i < allowedTypes.length; i++) {
                  flatAllowedTypes[i] = Utils.getComponentType(allowedTypes[i]);
               }

               boolean tryConverting = false;

               label58:
               for (Object object : delta) {
                  for (Class<?> allowedType : flatAllowedTypes) {
                     if (allowedType.isInstance(object)) {
                        continue label58;
                     }
                  }

                  tryConverting = true;
               }

               if (tryConverting) {
                  Object[] newDelta = new Object[delta.length];
                  Converters.convert(delta, newDelta, (Class<? extends Object>[])flatAllowedTypes);

                  for (Object object : newDelta) {
                     if (object == null) {
                        return null;
                     }
                  }

                  verifiedDelta = newDelta;
               }

               ExpressionPropertyHandler<Object, ?> handler = propertyInfo.handler();
               handler.change(propertyHaver, verifiedDelta, mode);
               return propertyHaver;
            } else {
               ExpressionPropertyHandler<Object, ?> handler = propertyInfo.handler();
               handler.change(propertyHaver, null, mode);
               return propertyHaver;
            }
         }
      };
      if (this.useCIP) {
         this.expr.changeInPlace(event, updateTypeFunction);
      } else {
         this.expr.stream(event).forEach(updateTypeFunction::apply);
      }
   }

   @Override
   public boolean isSingle() {
      return this.expr.isSingle();
   }

   @Override
   public Class<?> getReturnType() {
      return this.returnType;
   }

   @Override
   public Class<?>[] possibleReturnTypes() {
      return this.returnTypes;
   }

   @Override
   public String toString(Event event, boolean debug) {
      return this.getPropertyName() + " of " + this.expr.toString(event, debug);
   }

   class ChangeDetails extends EnumMap<Changer.ChangeMode, Map<Property.PropertyInfo<Handler>, Class<?>[]>> {
      public ChangeDetails() {
         super(Changer.ChangeMode.class);
      }

      public void storeTypes(Changer.ChangeMode mode, Property.PropertyInfo<Handler> propertyInfo, Class<?>[] types) {
         Map<Property.PropertyInfo<Handler>, Class<?>[]> map = this.computeIfAbsent(mode, k -> new HashMap<>());
         map.put(propertyInfo, types);
      }

      public Class<?>[] getTypes(Changer.ChangeMode mode, Property.PropertyInfo<Handler> propertyInfo) {
         Map<Property.PropertyInfo<Handler>, Class<?>[]> map = this.get(mode);
         return map != null ? map.get(propertyInfo) : null;
      }
   }
}
