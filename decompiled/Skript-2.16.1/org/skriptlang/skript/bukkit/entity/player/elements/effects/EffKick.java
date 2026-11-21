package org.skriptlang.skript.bukkit.entity.player.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Kick")
@Description({"Kicks a player from the server."})
@Example("on place of TNT, lava, or obsidian:\n\tkick the player due to \"You may not place %block%!\"\n\tcancel the event\n")
@Since({"1.0"})
public class EffKick extends Effect {
   private Expression<Player> players;
   @Nullable
   private Expression<Component> reason;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EFFECT,
         SyntaxInfo.builder(EffKick.class)
            .supplier(EffKick::new)
            .addPattern("kick %players% [(by reason of|because [of]|on account of|due to) %-textcomponent%]")
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.players = (Expression<Player>)exprs[0];
      this.reason = (Expression<Component>)exprs[1];
      return true;
   }

   @Override
   protected void execute(Event event) {
      Component reason = (Component)(this.reason == null ? Component.empty() : this.reason.getSingle(event));
      if (reason != null) {
         for (Player player : this.players.getArray(event)) {
            if (!Delay.isDelayed(event)) {
               if (event instanceof PlayerLoginEvent loginEvent && player.equals(loginEvent.getPlayer())) {
                  loginEvent.disallow(Result.KICK_OTHER, reason);
                  return;
               }

               if (event instanceof PlayerKickEvent kickEvent && player.equals(kickEvent.getPlayer())) {
                  kickEvent.leaveMessage(reason);
                  return;
               }
            }

            player.kick(reason);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug).append("kick", this.players).appendIf(this.reason != null, "on account of", this.reason).toString();
   }
}
