package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.Iterator;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.jetbrains.annotations.Nullable;

@Name("Fertilized Blocks")
@Description({"The blocks fertilized in block fertilize events."})
@RequiredPlugins({"Minecraft 1.13 or newer"})
@Events({"block fertilize"})
@Example("the fertilized blocks")
@Since({"2.5"})
public class ExprFertilizedBlocks extends SimpleExpression<BlockStateBlock> implements EventRestrictedSyntax {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(BlockFertilizeEvent.class);
   }

   @Nullable
   protected BlockStateBlock[] get(Event e) {
      return !(e instanceof BlockFertilizeEvent)
         ? null
         : ((BlockFertilizeEvent)e).getBlocks().stream().map(BlockStateBlock::new).toArray(BlockStateBlock[]::new);
   }

   @Nullable
   @Override
   public Iterator<? extends BlockStateBlock> iterator(Event e) {
      return !(e instanceof BlockFertilizeEvent) ? null : ((BlockFertilizeEvent)e).getBlocks().stream().map(BlockStateBlock::new).iterator();
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends BlockStateBlock> getReturnType() {
      return BlockStateBlock.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "the fertilized blocks";
   }

   static {
      if (Skript.classExists("org.bukkit.event.block.BlockFertilizeEvent")) {
         Skript.registerExpression(ExprFertilizedBlocks.class, BlockStateBlock.class, ExpressionType.SIMPLE, "[all] [the] fertilized blocks");
      }
   }
}
