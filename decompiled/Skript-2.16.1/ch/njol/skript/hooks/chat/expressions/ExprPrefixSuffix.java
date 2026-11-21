package ch.njol.skript.hooks.chat.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Prefix/Suffix")
@Description({"The prefix or suffix as defined in the server's chat plugin."})
@Example.Examples({@Example("on chat:\n\tcancel event\n\tbroadcast \"%player's prefix%%player's display name%%player's suffix%: %message%\" to the player's world\n"), @Example("set the player's prefix to \"[<red>Admin<reset>] \""), @Example("clear player's prefix")})
@Since({"2.0, 2.10 (delete)"})
@RequiredPlugins({"Vault", "a chat plugin that supports Vault"})
public class ExprPrefixSuffix extends SimplePropertyExpression<Player, String> {
   private boolean prefix;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.prefix = parseResult.mark == 1;
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   public String convert(Player player) {
      return this.prefix ? VaultHook.chat.getPlayerPrefix(player) : VaultHook.chat.getPlayerSuffix(player);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET -> new Class[]{String.class};
         case RESET, DELETE -> new Class[0];
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      CompletableFuture.runAsync(() -> {
         for (Player player : this.getExpr().getArray(event)) {
            switch (mode) {
               case SET:
                  if (this.prefix) {
                     VaultHook.chat.setPlayerPrefix(player, (String)delta[0]);
                  } else {
                     VaultHook.chat.setPlayerSuffix(player, (String)delta[0]);
                  }
                  break;
               case RESET:
               case DELETE:
                  if (this.prefix) {
                     VaultHook.chat.setPlayerPrefix(player, null);
                  } else {
                     VaultHook.chat.setPlayerSuffix(player, null);
                  }
            }
         }
      }).join();
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   protected String getPropertyName() {
      return this.prefix ? "prefix" : "suffix";
   }

   static {
      register(ExprPrefixSuffix.class, String.class, "[chat] (1:prefix|2:suffix)", "players");
   }
}
