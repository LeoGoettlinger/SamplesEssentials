package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;
import org.jetbrains.annotations.Nullable;

@Name("Language")
@Description({"Currently selected game language of a player. The value of the language is not defined properly.", "The vanilla Minecraft client will use lowercase language / country pairs separated by an underscore, but custom resource packs may use any format they wish."})
@Example("message player's current language")
@Since({"2.3"})
public class ExprLanguage extends SimplePropertyExpression<Player, String> {
   private static final boolean USE_DEPRECATED_METHOD = !Skript.methodExists(Player.class, "getLocale");
   @Nullable
   private static final MethodHandle getLocaleMethod;

   @Nullable
   public String convert(Player p) {
      if (USE_DEPRECATED_METHOD) {
         assert getLocaleMethod != null;

         try {
            return (String)getLocaleMethod.invoke((Spigot)p.spigot());
         } catch (Throwable var3) {
            Skript.exception(var3);
            return null;
         }
      } else {
         return p.getLocale();
      }
   }

   @Override
   protected String getPropertyName() {
      return "language";
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   static {
      register(ExprLanguage.class, String.class, "[([currently] selected|current)] [game] (language|locale) [setting]", "players");

      MethodHandle handle;
      try {
         handle = MethodHandles.lookup().findVirtual(Spigot.class, "getLocale", MethodType.methodType(String.class));
      } catch (IllegalAccessException | NoSuchMethodException var2) {
         handle = null;
      }

      getLocaleMethod = handle;
   }
}
