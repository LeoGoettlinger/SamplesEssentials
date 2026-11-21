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
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

@Name("Loaded Plugins")
@Description({"An expression to obtain a list of the names of the server's loaded plugins."})
@Example.Examples({@Example("if the loaded plugins contains \"Vault\":\n\tbroadcast \"This server uses Vault plugin!\"\n"), @Example("send \"Plugins (%size of loaded plugins%): %plugins%\" to player")})
@Since({"2.7"})
public class ExprPlugins extends SimpleExpression<String> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Nullable
   protected String[] get(Event e) {
      return Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).toArray(String[]::new);
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "the loaded plugins";
   }

   static {
      Skript.registerExpression(ExprPlugins.class, String.class, ExpressionType.SIMPLE, "[(all [[of] the]|the)] [loaded] plugins");
   }
}
