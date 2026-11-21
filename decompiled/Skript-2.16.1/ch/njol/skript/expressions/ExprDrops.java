package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
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
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Drops")
@Description({"This expression only works in death and harvest events.", "In a death event, will hold the drops of the dying creature.", "Drops can be prevented by removing them with \"remove ... from drops\", e.g. \"remove all pickaxes from the drops\", or \"clear drops\" if you don't want any drops at all."})
@Example.Examples({@Example("clear drops"), @Example("remove 4 planks from the drops")})
@Since({"1.0"})
@Events({"death"})
public class ExprDrops extends SimpleExpression<ItemType> implements EventRestrictedSyntax {
   private boolean isDeathEvent;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (this.getParser().isCurrentEvent(EntityDeathEvent.class)) {
         this.isDeathEvent = true;
      }

      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(EntityDeathEvent.class, PlayerHarvestBlockEvent.class);
   }

   @Nullable
   protected ItemType[] get(Event event) {
      if (event instanceof EntityDeathEvent entityDeathEvent) {
         return entityDeathEvent.getDrops().stream().map(ItemType::new).toArray(ItemType[]::new);
      } else if (event instanceof PlayerHarvestBlockEvent harvestBlockEvent) {
         return harvestBlockEvent.getItemsHarvested().stream().map(ItemType::new).toArray(ItemType[]::new);
      } else {
         assert false;

         return new ItemType[0];
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.getParser().getHasDelayBefore().isTrue()) {
         Skript.error("Can't change the drops after the event has already passed");
         return null;
      } else {
         return switch (mode) {
            case SET, ADD, REMOVE -> this.isDeathEvent
               ? (Class[])CollectionUtils.array(ItemType[].class, Inventory[].class, Experience[].class)
               : (Class[])CollectionUtils.array(ItemType[].class, Inventory[].class);
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      List<ItemStack> drops = null;
      int originalExperience = 0;
      if (event instanceof EntityDeathEvent entityDeathEvent) {
         drops = entityDeathEvent.getDrops();
         originalExperience = entityDeathEvent.getDroppedExp();
      } else {
         if (!(event instanceof PlayerHarvestBlockEvent harvestBlockEvent)) {
            return;
         }

         drops = harvestBlockEvent.getItemsHarvested();
      }

      assert delta != null;

      int deltaExperience = -1;
      boolean removeAllExperience = false;
      List<ItemType> deltaDrops = new ArrayList<>();

      for (Object object : delta) {
         if (!(object instanceof Experience experience)) {
            if (object instanceof Inventory inventory) {
               for (ItemStack item : inventory.getContents()) {
                  if (item != null) {
                     deltaDrops.add(new ItemType(item));
                  }
               }
            } else if (object instanceof ItemType itemType) {
               deltaDrops.add(itemType);
            } else {
               assert false;
            }
         } else {
            if (experience.getInternalXP() == -1 && mode == Changer.ChangeMode.REMOVE || mode == Changer.ChangeMode.REMOVE_ALL) {
               removeAllExperience = true;
            }

            if (deltaExperience == -1) {
               deltaExperience = experience.getXP();
            } else {
               deltaExperience += experience.getXP();
            }
         }
      }

      if (deltaExperience > -1 && event instanceof EntityDeathEvent entityDeathEvent) {
         switch (mode) {
            case SET:
               entityDeathEvent.setDroppedExp(deltaExperience);
               break;
            case ADD:
               entityDeathEvent.setDroppedExp(originalExperience + deltaExperience);
               break;
            case REMOVE:
               entityDeathEvent.setDroppedExp(originalExperience - deltaExperience);
            case REMOVE_ALL:
               if (removeAllExperience) {
                  entityDeathEvent.setDroppedExp(0);
               }
               break;
            case DELETE:
            case RESET:
               assert false;
         }
      }

      if (!deltaDrops.isEmpty()) {
         switch (mode) {
            case SET:
               drops.clear();
            case ADD:
               for (ItemType itemx : deltaDrops) {
                  itemx.addTo(drops);
               }
               break;
            case REMOVE:
               for (ItemType itemx : deltaDrops) {
                  itemx.removeFrom(false, drops);
               }
               break;
            case REMOVE_ALL:
               for (ItemType itemx : deltaDrops) {
                  itemx.removeAll(false, drops);
               }
               break;
            case DELETE:
            case RESET:
               assert false;
         }
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
   public String toString(@Nullable Event event, boolean debug) {
      return "the drops";
   }

   static {
      Skript.registerExpression(ExprDrops.class, ItemType.class, ExpressionType.SIMPLE, "[the] drops");
   }
}
