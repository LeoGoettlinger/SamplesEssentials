package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
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
import java.util.List;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.jetbrains.annotations.Nullable;

@Name("Absorbed blocks")
@Description({"The blocks absorbed by a sponge block."})
@Events({"sponge absorb"})
@Example("the absorbed blocks")
@Since({"2.5"})
public class ExprAbsorbedBlocks extends SimpleExpression<BlockStateBlock> implements EventRestrictedSyntax {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(SpongeAbsorbEvent.class);
   }

   @Nullable
   protected BlockStateBlock[] get(Event e) {
      if (!(e instanceof SpongeAbsorbEvent)) {
         return null;
      } else {
         List<BlockState> bs = ((SpongeAbsorbEvent)e).getBlocks();
         return bs.stream().map(BlockStateBlock::new).toArray(BlockStateBlock[]::new);
      }
   }

   @Nullable
   @Override
   public Iterator<BlockStateBlock> iterator(Event e) {
      if (!(e instanceof SpongeAbsorbEvent)) {
         return null;
      } else {
         List<BlockState> bs = ((SpongeAbsorbEvent)e).getBlocks();
         return bs.stream().map(BlockStateBlock::new).iterator();
      }
   }

   @Override
   public Class<? extends BlockStateBlock> getReturnType() {
      return BlockStateBlock.class;
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "absorbed blocks";
   }

   static {
      Skript.registerExpression(ExprAbsorbedBlocks.class, BlockStateBlock.class, ExpressionType.SIMPLE, "[the] absorbed blocks");
   }
}
