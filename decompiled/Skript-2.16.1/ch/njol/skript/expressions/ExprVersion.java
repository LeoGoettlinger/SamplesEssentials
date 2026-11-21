package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.lang.invoke.StringConcatFactory;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Version")
@Description({"The version of Bukkit, Minecraft or Skript respectively."})
@Example.Examples({@Example("message \"This server is running Minecraft %minecraft version% on Bukkit %bukkit version%\""), @Example("message \"This server is powered by Skript %skript version%\"")})
@Since({"2.0"})
public class ExprVersion extends SimpleExpression<String> {
   private ExprVersion.VersionType type;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.type = ExprVersion.VersionType.values()[parseResult.mark];
      return true;
   }

   protected String[] get(Event e) {
      return new String[]{this.type.get()};
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return this.type + " version";
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   static {
      Skript.registerExpression(ExprVersion.class, String.class, ExpressionType.SIMPLE, "(0¦[craft]bukkit|1¦minecraft|2¦skript)( |-)version");
   }

   private static enum VersionType {
      BUKKIT("Bukkit") {
         @Override
         public String get() {
            return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(Bukkit.getBukkitVersion());
         }
      },
      MINECRAFT("Minecraft") {
         @Override
         public String get() {
            return Skript.getMinecraftVersion().toString();
         }
      },
      SKRIPT("Skript") {
         @Override
         public String get() {
            return Skript.getVersion().toString();
         }
      };

      private final String name;

      private VersionType(final String name) {
         this.name = name;
      }

      @Override
      public String toString() {
         return this.name;
      }

      public abstract String get();
   }
}
