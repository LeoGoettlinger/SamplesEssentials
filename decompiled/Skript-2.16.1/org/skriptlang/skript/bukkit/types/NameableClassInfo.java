package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import net.kyori.adventure.text.Component;
import org.bukkit.Nameable;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Internal
public class NameableClassInfo extends ClassInfo<Nameable> {
   public NameableClassInfo() {
      super(Nameable.class, "nameable");
      this.user(new String[]{"nameables?"})
         .name("Nameable")
         .description("A variety of Bukkit types that can have names, such as entities and some blocks.")
         .since("2.13")
         .defaultExpression(new EventValueExpression<>(Nameable.class))
         .after("entity", "commandsender", "block", "player")
         .property(
            Property.NAME,
            "The name of the nameable, if it has one, as text. Use 'display name' if you need a changeable name.",
            Skript.instance(),
            ExpressionPropertyHandler.of(nameable -> nameable instanceof CommandSender sender ? sender.name() : nameable.customName(), Component.class)
         )
         .property(
            Property.DISPLAY_NAME,
            "The custom name of the nameable, if it has one, as text. Can be set or reset.",
            Skript.instance(),
            new NameableClassInfo.NameableNameHandler()
         );
   }

   private static class NameableNameHandler implements ExpressionPropertyHandler<Nameable, Component> {
      public Component convert(Nameable nameable) {
         return nameable.customName();
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET ? null : new Class[]{Component.class};
      }

      public void change(Nameable nameable, @Nullable Object[] delta, Changer.ChangeMode mode) {
         assert mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET;

         Component name = delta == null ? null : (Component)delta[0];
         nameable.customName(name);
      }

      @NotNull
      @Override
      public Class<Component> returnType() {
         return Component.class;
      }
   }
}
