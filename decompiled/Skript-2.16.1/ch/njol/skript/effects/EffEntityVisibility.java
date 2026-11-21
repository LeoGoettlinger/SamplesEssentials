package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprHiddenPlayers;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Entity Visibility")
@Description({"Change visibility of the given entities for the given players.", "If no players are given, will hide the entities from all online players.", "", "When reveal is used in combination of the <a href='#ExprHiddenPlayers'>hidden players</a> expression and the viewers are not specified, this will default it to the given player in the hidden players expression.", "", "Note: all previously hidden entities (including players) will be visible when a player leaves and rejoins."})
@Example.Examples({@Example("on spawn:\n\tif event-entity is a chicken:\n\t\thide event-entity\n"), @Example("reveal hidden players of players")})
@Since({"2.3, 2.10 (entities)"})
@RequiredPlugins({"Minecraft 1.19+ (entities)"})
public class EffEntityVisibility extends Effect {
   private boolean reveal;
   @UnknownNullability
   private Expression<Entity> hidden;
   @UnknownNullability
   private Expression<Player> viewers;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult result) {
      this.reveal = matchedPattern == 1;
      this.hidden = (Expression<Entity>)exprs[0];
      if (this.reveal && exprs[0] instanceof ExprHiddenPlayers && exprs.length == 1) {
         this.viewers = ((ExprHiddenPlayers)exprs[0]).getViewers();
      } else {
         this.viewers = (Expression<Player>)exprs[1];
      }

      return true;
   }

   @Override
   protected void execute(Event event) {
      Player[] updated = this.viewers != null ? this.viewers.getArray(event) : Bukkit.getOnlinePlayers().toArray(new Player[0]);
      Skript instance = Skript.getInstance();
      if (this.reveal) {
         for (Player player : updated) {
            for (Entity entity : this.hidden.getArray(event)) {
               player.showEntity(instance, entity);
            }
         }
      } else {
         for (Player player : updated) {
            for (Entity entity : this.hidden.getArray(event)) {
               player.hideEntity(instance, entity);
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return (this.reveal ? "reveal " : "hide ")
         + "entities "
         + this.hidden.toString(event, debug)
         + (this.reveal ? " to " : " from ")
         + this.viewers.toString(event, debug);
   }

   static {
      Skript.registerEffect(EffEntityVisibility.class, "hide %entities% [(from|for) %-players%]", "reveal %entities% [(to|for|from) %-players%]");
   }
}
