package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;

@Name("Tool")
@Description({"The item an entity is holding in their main or off hand."})
@Example.Examples({@Example("player's tool is tagged with minecraft tag \"pickaxes\""), @Example("player's off hand tool is a shield"), @Example("set tool of all players to a diamond sword"), @Example("set offhand tool of target entity to a bow")})
@Since({"1.0"})
public class ExprTool extends PropertyExpression<LivingEntity, Slot> {
   private boolean offHand;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.setExpr((Expression<? extends LivingEntity>)exprs[0]);
      this.offHand = matchedPattern >= 2;
      return true;
   }

   protected Slot[] get(Event event, LivingEntity[] source) {
      boolean delayed = Delay.isDelayed(event);
      return this.get(source, entity -> {
         if (!delayed) {
            if (!this.offHand && event instanceof PlayerItemHeldEvent playerItemHeldEvent && playerItemHeldEvent.getPlayer() == entity) {
               PlayerInventory inventory = playerItemHeldEvent.getPlayer().getInventory();
               return new InventorySlot(inventory, this.getTime() >= 0 ? playerItemHeldEvent.getNewSlot() : playerItemHeldEvent.getPreviousSlot());
            }

            if (event instanceof PlayerBucketEvent playerBucketEvent && playerBucketEvent.getPlayer() == entity) {
               PlayerInventory inventory = playerBucketEvent.getPlayer().getInventory();
               boolean isOffHand = this.offHand || playerBucketEvent.getHand() == EquipmentSlot.OFF_HAND;
               int inventorySlot = isOffHand ? BukkitUtils.getEquipmentSlotIndex(EquipmentSlot.OFF_HAND) : inventory.getHeldItemSlot();
               return new InventorySlot(inventory, inventorySlot) {
                  @Override
                  public ItemStack getItem() {
                     return ExprTool.this.getTime() <= 0 ? super.getItem() : playerBucketEvent.getItemStack();
                  }

                  @Override
                  public void setItem(@Nullable ItemStack item) {
                     if (ExprTool.this.getTime() >= 0) {
                        playerBucketEvent.setItemStack(item);
                     } else {
                        super.setItem(item);
                     }
                  }
               };
            }
         }

         EntityEquipment equipment = entity.getEquipment();
         return equipment == null ? null : new ch.njol.skript.util.slot.EquipmentSlot(equipment, this.offHand ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND) {
            @Override
            public String toString(@Nullable Event event, boolean debug) {
               SyntaxStringBuilder syntaxBuilder = new SyntaxStringBuilder(event, debug);
               switch (EventValue.Time.of(ExprTool.this.getTime())) {
                  case FUTURE:
                     syntaxBuilder.append("future");
                     break;
                  case PAST:
                     syntaxBuilder.append("former");
               }

               if (ExprTool.this.offHand) {
                  syntaxBuilder.append("off hand");
               }

               syntaxBuilder.append("tool of", Classes.toString(this.getItem()));
               return syntaxBuilder.toString();
            }
         };
      });
   }

   @Override
   public Class<Slot> getReturnType() {
      return Slot.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder syntaxBuilder = new SyntaxStringBuilder(event, debug);
      if (this.offHand) {
         syntaxBuilder.append("off hand");
      }

      syntaxBuilder.append("tool of", this.getExpr());
      return syntaxBuilder.toString();
   }

   @Override
   public boolean setTime(int time) {
      return super.setTime(time, this.getExpr(), new Class[]{PlayerItemHeldEvent.class, PlayerBucketEvent.class});
   }

   static {
      Skript.registerExpression(
         ExprTool.class,
         Slot.class,
         ExpressionType.PROPERTY,
         "[the] (tool|held item|weapon) [of %livingentities%]",
         "%livingentities%'[s] (tool|held item|weapon)",
         "[the] off[ ]hand (tool|item) [of %livingentities%]",
         "%livingentities%'[s] off[ ]hand (tool|item)"
      );
   }
}
