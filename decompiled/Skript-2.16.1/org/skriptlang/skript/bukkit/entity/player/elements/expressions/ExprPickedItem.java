package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import io.papermc.paper.event.player.PlayerPickEntityEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import java.util.Locale;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Picked Item/Block/Entity")
@Description({"The item, block, or entity picked by a player using the pick block key (default middle mouse button)."})
@Example("on player pick item:\n\tsend \"You picked %the picked item%!\" to the player\n")
@Since({"2.15"})
@RequiredPlugins({"1.21.5+"})
@Keywords({"pick", "picked", "picked item", "picked block", "picked entity"})
public class ExprPickedItem extends SimpleExpression<Object> implements EventRestrictedSyntax {
   private ExprPickedItem.PickType pickType;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                     ExprPickedItem.class, Object.class
                  )
                  .supplier(ExprPickedItem::new))
               .addPattern("[the] picked (item|1:block|2:entity)"))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.pickType = ExprPickedItem.PickType.values()[parseResult.mark];
      return true;
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      return (Object[])(switch (this.pickType) {
         case ITEM -> event instanceof PlayerPickBlockEvent pickBlockEvent
            ? new ItemType[]{new ItemType(pickBlockEvent.getBlock())}
            : (event instanceof PlayerPickEntityEvent pickEntityEvent ? new ItemType[]{new ItemType(pickEntityEvent.getEntity().getPickItemStack())} : null);
         case BLOCK -> event instanceof PlayerPickBlockEvent pickBlockEvent ? new Block[]{pickBlockEvent.getBlock()} : null;
         case ENTITY -> event instanceof PlayerPickEntityEvent pickEntityEvent ? new Entity[]{pickEntityEvent.getEntity()} : null;
      });
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<?> getReturnType() {
      return switch (this.pickType) {
         case ITEM -> ItemType.class;
         case BLOCK -> Block.class;
         case ENTITY -> Entity.class;
      };
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(PlayerPickItemEvent.class);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the picked " + this.pickType.name().toLowerCase(Locale.ENGLISH);
   }

   private static enum PickType {
      ITEM,
      BLOCK,
      ENTITY;
   }
}
