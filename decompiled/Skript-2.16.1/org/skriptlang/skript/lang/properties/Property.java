package org.skriptlang.skript.lang.properties;

import ch.njol.skript.Skript;
import java.util.Locale;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.properties.handlers.ContainsHandler;
import org.skriptlang.skript.lang.properties.handlers.TypedValueHandler;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

@Experimental
public record Property<Handler extends PropertyHandler<?>>(
   String name, String description, String[] since, SkriptAddon provider, @NotNull Class<? extends Handler> handler
) {
   private static final PropertyRegistry PROPERTY_REGISTRY = Skript.getAddonInstance().registry(PropertyRegistry.class);
   public static final Property<ExpressionPropertyHandler<?, ?>> NAME = of(
      "name", "A name, such as a script's name or a player's account name.", "2.13", Skript.instance(), ExpressionPropertyHandler.class
   );
   public static final Property<ExpressionPropertyHandler<?, ?>> DISPLAY_NAME = of(
      "display name",
      "A more prominently displayed name, such as a player's display name or an entity's custom name. Often more easily changed than the regular name.",
      "2.13",
      Skript.instance(),
      ExpressionPropertyHandler.class
   );
   public static final Property<ContainsHandler<?, ?>> CONTAINS = of(
      "contains", "Something that can contain other things, such as an inventory or a string.", "2.13", Skript.instance(), ContainsHandler.class
   );
   public static final Property<ExpressionPropertyHandler<?, ?>> AMOUNT = of(
      "amount", "The amount of something, say the number of items in a stack or in a queue.", "2.13", Skript.instance(), ExpressionPropertyHandler.class
   );
   public static final Property<ExpressionPropertyHandler<?, ?>> SIZE = of(
      "size", "The size of something, say the number of elements in a queue.", "2.13", Skript.instance(), ExpressionPropertyHandler.class
   );
   public static final Property<ExpressionPropertyHandler<?, ?>> SCALE = of(
      "scale", "The scale of something, say the x/y/z scales of a display entity.", "2.14", Skript.instance(), ExpressionPropertyHandler.class
   );
   public static final Property<ExpressionPropertyHandler<?, ?>> NUMBER = of(
      "number", "The number of something, say the number of elements in a queue.", "2.13", Skript.instance(), ExpressionPropertyHandler.class
   );
   public static final Property<ConditionPropertyHandler<?>> IS_EMPTY = of(
      "empty", "Whether something is empty or not.", "2.13", Skript.instance(), ConditionPropertyHandler.class
   );
   public static final Property<TypedValueHandler<?, ?>> TYPED_VALUE = of(
      "typed value", "A value of a specific type, e.g. 'string value of x'.", "2.13", Skript.instance(), TypedValueHandler.class
   );
   public static final Property<WXYZHandler<?, ?>> WXYZ = of(
      "wxyz component",
      "The W, X, Y, or Z components of something, e.g. the x coordinate of a location or vector.",
      "2.14",
      Skript.instance(),
      WXYZHandler.class
   );
   public static final Property<ExpressionPropertyHandler<?, ?>> SPEED = of(
      "speed", "The speed at which something is moving.", "2.14", Skript.instance(), ExpressionPropertyHandler.class
   );
   public static final Property<ExpressionPropertyHandler<?, ?>> TITLE = of(
      "title", "The title of something.", "2.16", Skript.instance(), ExpressionPropertyHandler.class
   );
   public static final Property<ExpressionPropertyHandler<?, ?>> PROGRESS = of(
      "progress", "The progress of something.", "2.16", Skript.instance(), ExpressionPropertyHandler.class
   );
   public static final Property<ExpressionPropertyHandler<?, ?>> STYLE = of(
      "style", "The style of something.", "2.16", Skript.instance(), ExpressionPropertyHandler.class
   );
   public static final Property<ExpressionPropertyHandler<?, ?>> VIEWERS = of(
      "viewers", "The viewers of something.", "2.16", Skript.instance(), ExpressionPropertyHandler.class
   );

   public Property(String name, String description, String[] since, SkriptAddon provider, @NotNull Class<? extends Handler> handler) {
      this.name = name.toLowerCase(Locale.ENGLISH);
      this.description = description;
      this.since = since;
      this.provider = provider;
      this.handler = handler;
   }

   public String getDocumentationID() {
      return this.name.replace(' ', '-').toLowerCase(Locale.ENGLISH);
   }

   private void register() {
      PROPERTY_REGISTRY.register(this);
   }

   @Contract("_, _, _, _, _ -> new")
   @NotNull
   public static <HandlerClass extends PropertyHandler<?>, Handler extends HandlerClass> Property<Handler> of(
      @NotNull String name, @NotNull String description, @NotNull String since, @NotNull SkriptAddon provider, @NotNull Class<HandlerClass> handler
   ) {
      return new Property<>(name, description, new String[]{since}, provider, handler);
   }

   @Contract("_, _, _, _, _ -> new")
   @NotNull
   public static <HandlerClass extends PropertyHandler<?>, Handler extends HandlerClass> Property<Handler> of(
      @NotNull String name, @NotNull String description, @NotNull String[] since, @NotNull SkriptAddon provider, @NotNull Class<HandlerClass> handler
   ) {
      return new Property<>(name, description, since, provider, handler);
   }

   public static void registerDefaultProperties() {
      AMOUNT.register();
      CONTAINS.register();
      DISPLAY_NAME.register();
      IS_EMPTY.register();
      NAME.register();
      NUMBER.register();
      PROGRESS.register();
      SCALE.register();
      SIZE.register();
      SPEED.register();
      STYLE.register();
      TITLE.register();
      TYPED_VALUE.register();
      VIEWERS.register();
   }

   public record PropertyInfo<Handler extends PropertyHandler<?>>(Property<Handler> property, Handler handler) {
   }
}
