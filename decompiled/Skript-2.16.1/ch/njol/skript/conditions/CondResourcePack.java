package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.jetbrains.annotations.Nullable;

@Name("Resource Pack")
@Description({"Checks state of the resource pack in a <a href='#resource_pack_request_action'>resource pack request response</a> event."})
@Example("on resource pack response:\n\tif the resource pack wasn't accepted:\n\t\tkick the player due to \"You have to install the resource pack to play in this server!\"\n")
@Since({"2.4"})
@Events({"resource pack request response"})
public class CondResourcePack extends Condition implements EventRestrictedSyntax {
   private Expression<Status> states;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.states = (Expression<Status>)exprs[0];
      this.setNegated(matchedPattern == 1);
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(PlayerResourcePackStatusEvent.class);
   }

   @Override
   public boolean check(Event e) {
      if (!(e instanceof PlayerResourcePackStatusEvent)) {
         return this.isNegated();
      } else {
         Status state = ((PlayerResourcePackStatusEvent)e).getStatus();
         return this.states.check(e, state::equals, this.isNegated());
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "resource pack was " + (this.isNegated() ? "not " : "") + this.states.toString(e, debug);
   }

   static {
      Skript.registerCondition(
         CondResourcePack.class,
         "[the] resource pack (was|is|has) [been] %resourcepackstate%",
         "[the] resource pack (was|is|has)(n't| not) [been] %resourcepackstate%"
      );
   }
}
