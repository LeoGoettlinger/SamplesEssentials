package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.GameMode;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerGameModeChange extends SkriptEvent {
   private Literal<GameMode> gamemode;

   public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry registry) {
      syntaxRegistry.register(
         BukkitSyntaxInfos.Event.KEY,
         ((BukkitSyntaxInfos.Event.Builder)((BukkitSyntaxInfos.Event.Builder)BukkitSyntaxInfos.Event.builder(
                     EvtPlayerGameModeChange.class, "Player GameMode Change"
                  )
                  .supplier(EvtPlayerGameModeChange::new))
               .addEvent(PlayerGameModeChangeEvent.class)
               .addPatterns(new String[]{"[player] game[ ]mode change [to %gamemode%]"}))
            .addDescription("Called when a player's gamemode is changed. See <a href='#gamemode'>gamemode</a>.\n")
            .addExample(
               "on game mode change:\n\tif event-gamemode is creative:\n\t\tsend \"Your gamemode is now creative! Totally don't go spawning items in..\"\n"
            )
            .addExample("on game mode change:\n\tsend \"Wow! Your gamemode changed!\"\n")
            .addSince("1.0")
            .build()
      );
      registry.register(
         EventValue.builder(PlayerGameModeChangeEvent.class, GameMode.class).getter(PlayerGameModeChangeEvent::getNewGameMode).patterns("gamemode").build()
      );
   }

   @Override
   public boolean init(Literal<?>[] literals, int i, SkriptParser.ParseResult parseResult) {
      this.gamemode = (Literal<GameMode>)literals[0];
      return true;
   }

   @Override
   public boolean check(Event event) {
      if (this.gamemode == null) {
         return true;
      } else {
         GameMode gamemode = this.gamemode.getSingle(event);
         PlayerGameModeChangeEvent playerEvent = (PlayerGameModeChangeEvent)event;
         return playerEvent.getNewGameMode().equals(gamemode);
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug).append("player gamemode change").appendIf(this.gamemode != null, "to", this.gamemode).toString();
   }
}
