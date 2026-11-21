package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Drops Of Block")
@Description({"A list of the items that will drop when a block is broken."})
@RequiredPlugins({"Minecraft 1.15+ ('as %entity%')"})
@Example("on break of block:\n\tgive drops of block using player's tool to player\n")
@Since({"2.5.1"})
public class ExprDropsOfBlock extends SimpleExpression<ItemType> {
   private static final boolean DROPS_OF_ENTITY_EXISTS = Skript.methodExists(Block.class, "getDrops", ItemStack.class, Entity.class);
   private Expression<Block> block;
   private Expression<ItemType> item;
   private Expression<Entity> entity;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.block = (Expression<Block>)exprs[0];
      this.item = (Expression<ItemType>)exprs[1];
      if (!DROPS_OF_ENTITY_EXISTS && parseResult.mark == 1) {
         Skript.error("Getting the drops of a block as an entity is only possible on Minecraft 1.15+", ErrorQuality.SEMANTIC_ERROR);
         return false;
      } else {
         this.entity = (Expression<Entity>)exprs[2];
         return true;
      }
   }

   @Nullable
   protected ItemType[] get(Event e) {
      Block[] blocks = this.block.getArray(e);
      if (this.block == null) {
         return null;
      } else if (this.item == null) {
         ArrayList<ItemType> list = new ArrayList<>();

         for (Block block : blocks) {
            ItemStack[] drops = block.getDrops().toArray(new ItemStack[0]);

            for (ItemStack drop : drops) {
               list.add(new ItemType(drop));
            }
         }

         return list.toArray(new ItemType[0]);
      } else if (this.entity != null) {
         ItemType item = this.item.getSingle(e);
         Entity entity = this.entity.getSingle(e);
         ArrayList<ItemType> list = new ArrayList<>();

         for (Block block : blocks) {
            ItemStack[] drops = block.getDrops(item.getRandom(), entity).toArray(new ItemStack[0]);

            for (ItemStack drop : drops) {
               list.add(new ItemType(drop));
            }
         }

         return list.toArray(new ItemType[0]);
      } else {
         ItemType item = this.item.getSingle(e);
         ArrayList<ItemType> list = new ArrayList<>();

         for (Block block : blocks) {
            ItemStack[] drops = block.getDrops(item.getRandom()).toArray(new ItemStack[0]);

            for (ItemStack drop : drops) {
               list.add(new ItemType(drop));
            }
         }

         return list.toArray(new ItemType[0]);
      }
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends ItemType> getReturnType() {
      return ItemType.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "drops of "
         + this.block.toString(e, debug)
         + (this.item != null ? " using " + this.item.toString(e, debug) + (this.entity != null ? " as " + this.entity.toString(e, debug) : null) : "");
   }

   static {
      Skript.registerExpression(
         ExprDropsOfBlock.class,
         ItemType.class,
         ExpressionType.COMBINED,
         "[(all|the|all [of] the)] drops of %blocks% [(using|with) %-itemtype% [(1¦as %-entity%)]]",
         "%blocks%'s drops [(using|with) %-itemtype% [(1¦as %-entity%)]]"
      );
   }
}
