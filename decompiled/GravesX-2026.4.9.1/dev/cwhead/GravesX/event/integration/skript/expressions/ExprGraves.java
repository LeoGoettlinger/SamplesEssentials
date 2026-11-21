package dev.cwhead.GravesX.event.integration.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.api.grave.GraveManagementAPI;
import dev.cwhead.GravesX.integration.SkriptImpl;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.Builder;

@Name("GravesX Graves")
@Description({"Gets loaded graves, optionally filtered by player, through the GravesX grave management API."})
@Examples({"loop loaded graves of player:", "\tbroadcast \"Grave: %loop-value%, UUID: %grave uuid of loop-value%\""})
public class ExprGraves extends SimpleExpression<Grave> {
   private Expression<Player> player;

   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      if (matchedPattern == 1 || matchedPattern == 2) {
         this.player = (Expression<Player>)expressions[0];
      }

      return true;
   }

   @Nullable
   protected Grave[] get(Event event) {
      GraveManagementAPI api = SkriptImpl.getGraveManagementAPI();
      Player playerValue = this.player != null ? (Player)this.player.getSingle(event) : null;
      List<Grave> graves = api.getGraves(playerValue);
      return graves.toArray(new Grave[0]);
   }

   public boolean isSingle() {
      return false;
   }

   public Class<? extends Grave> getReturnType() {
      return Grave.class;
   }

   public String toString(@Nullable Event event, boolean debug) {
      return this.player != null ? "loaded graves of " + this.player : "loaded graves";
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            SyntaxRegistry.EXPRESSION,
            ((Builder)org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder(ExprGraves.class, Grave.class)
                  .addPatterns(new String[]{"[the] loaded graves", "[the] loaded graves of %player%", "[the] graves of %player%"}))
               .build()
         );
   }
}
