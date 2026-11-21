package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Beehive Target Flower")
@Description({"The flower a beehive has selected to pollinate from."})
@Example.Examples({@Example("set the target flower of {_beehive} to location(0, 0, 0)"), @Example("clear the target flower of {_beehive}")})
@Since({"2.11"})
public class ExprBeehiveFlower extends SimplePropertyExpression<Block, Location> {
   @Nullable
   public Location convert(Block block) {
      return block.getState() instanceof Beehive beehive ? beehive.getFlower() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.DELETE ? null : CollectionUtils.array(Location.class, Block.class);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Location location = null;
      if (delta != null) {
         if (delta[0] instanceof Location loc) {
            location = loc;
         } else if (delta[0] instanceof Block block) {
            location = block.getLocation();
         }
      }

      for (Block block : this.getExpr().getArray(event)) {
         if (block.getState() instanceof Beehive beehive) {
            beehive.setFlower(location);
            beehive.update(true, false);
         }
      }
   }

   @Override
   public Class<Location> getReturnType() {
      return Location.class;
   }

   @Override
   protected String getPropertyName() {
      return "target flower";
   }

   static {
      registerDefault(ExprBeehiveFlower.class, Location.class, "target flower", "blocks");
   }
}
