package org.skriptlang.skript.bukkit.block.furnace.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Furnace Slot")
@Description({"A slot of a furnace, i.e. either the ore, fuel or result slot."})
@Example.Examples({@Example("set the fuel slot of the clicked block to a lava bucket"), @Example("set the block's ore slot to 64 iron ore"), @Example("clear the result slot of the block"), @Example("on smelt:\n\tif the fuel slot is charcoal:\n\t\tadd 5 seconds to the burn time\n")})
@Events({"smelt", "fuel burn"})
@Since({"1.0, 2.8.0 (syntax rework)"})
public class ExprFurnaceSlot extends SimpleExpression<Slot> {
   private static final ExprFurnaceSlot.FurnaceSlot[] furnaceSlots = ExprFurnaceSlot.FurnaceSlot.values();
   @Nullable
   private Expression<Block> blocks;
   private ExprFurnaceSlot.FurnaceSlot selectedSlot;
   private boolean isEvent;

   public static void register(SyntaxRegistry registry) {
      String[] patterns = new String[furnaceSlots.length * 2];

      for (ExprFurnaceSlot.FurnaceSlot slot : furnaceSlots) {
         patterns[2 * slot.ordinal()] = "[the] " + slot.pattern + " slot[s] [of %blocks%]";
         patterns[2 * slot.ordinal() + 1] = "%blocks%'[s] " + slot.pattern + " slot[s]";
      }

      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprFurnaceSlot.class, Slot.class
                     )
                     .addPatterns(patterns))
                  .supplier(ExprFurnaceSlot::new))
               .priority(PropertyExpression.DEFAULT_PRIORITY))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.selectedSlot = furnaceSlots[(int)Math2.floor(matchedPattern / 2)];
      if (exprs[0] != null) {
         this.blocks = (Expression<Block>)exprs[0];
      } else {
         if (!this.getParser().isCurrentEvent(FurnaceBurnEvent.class, FurnaceStartSmeltEvent.class, FurnaceExtractEvent.class, FurnaceSmeltEvent.class)) {
            Skript.error("There's no furnace in a " + this.getParser().getCurrentEventName() + " event.");
            return false;
         }

         this.isEvent = true;
      }

      return true;
   }

   @Nullable
   protected Slot[] get(Event event) {
      Block[] blocks;
      if (this.isEvent) {
         blocks = new Block[1];
         if (!(event instanceof BlockEvent blockEvent)) {
            return new Slot[0];
         }

         blocks[0] = blockEvent.getBlock();
      } else {
         assert this.blocks != null;

         blocks = this.blocks.getArray(event);
      }

      List<Slot> slots = new ArrayList<>();

      for (Block block : blocks) {
         BlockState state = block.getState();
         if (state instanceof Furnace) {
            FurnaceInventory furnaceInventory = ((Furnace)state).getInventory();
            if (this.isEvent && !Delay.isDelayed(event)) {
               slots.add(new ExprFurnaceSlot.FurnaceEventSlot(event, furnaceInventory));
            } else {
               slots.add(new InventorySlot(furnaceInventory, this.selectedSlot.ordinal()));
            }
         }
      }

      return slots.toArray(new Slot[0]);
   }

   @Override
   public boolean isSingle() {
      if (this.isEvent) {
         return true;
      } else {
         assert this.blocks != null;

         return this.blocks.isSingle();
      }
   }

   @Override
   public Class<? extends Slot> getReturnType() {
      return InventorySlot.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.selectedSlot.toString + " slot of " + (this.isEvent ? event.getEventName() : this.blocks.toString(event, debug));
   }

   @Override
   public boolean setTime(int time) {
      if (this.isEvent) {
         return this.selectedSlot == ExprFurnaceSlot.FurnaceSlot.FUEL
            ? this.setTime(time, FurnaceBurnEvent.class)
            : this.setTime(time, FurnaceSmeltEvent.class);
      } else {
         return false;
      }
   }

   private final class FurnaceEventSlot extends InventorySlot {
      private final Event event;

      public FurnaceEventSlot(Event event, FurnaceInventory furnaceInventory) {
         super(furnaceInventory, ExprFurnaceSlot.this.selectedSlot.ordinal());
         this.event = event;
      }

      @Nullable
      @Override
      public ItemStack getItem() {
         return switch (ExprFurnaceSlot.this.selectedSlot) {
            case INPUT -> {
               if (this.event instanceof FurnaceSmeltEvent furnaceSmeltEvent) {
                  ItemStack source = furnaceSmeltEvent.getSource().clone();
                  if (ExprFurnaceSlot.this.getTime() != EventValues.TIME_FUTURE) {
                     yield source;
                  } else {
                     source.setAmount(source.getAmount() - 1);
                     yield source;
                  }
               } else {
                  yield super.getItem();
               }
            }
            case FUEL -> {
               if (this.event instanceof FurnaceBurnEvent furnaceBurnEvent) {
                  ItemStack fuel = furnaceBurnEvent.getFuel().clone();
                  if (ExprFurnaceSlot.this.getTime() != EventValues.TIME_FUTURE) {
                     yield fuel;
                  } else {
                     Material newMaterial = fuel.getType() == Material.LAVA_BUCKET ? Material.BUCKET : Material.AIR;
                     fuel.setAmount(fuel.getAmount() - 1);
                     if (fuel.getAmount() == 0) {
                        fuel = new ItemStack(newMaterial);
                     }

                     yield fuel;
                  }
               } else {
                  yield super.getItem();
               }
            }
            case OUTPUT -> {
               if (this.event instanceof FurnaceSmeltEvent furnaceSmeltEvent) {
                  ItemStack result = furnaceSmeltEvent.getResult().clone();
                  ItemStack currentResult = ((FurnaceInventory)this.getInventory()).getResult();
                  if (currentResult != null) {
                     currentResult = currentResult.clone();
                  }

                  if (ExprFurnaceSlot.this.getTime() != EventValues.TIME_FUTURE) {
                     yield currentResult;
                  } else if (currentResult != null && currentResult.isSimilar(result)) {
                     currentResult.setAmount(currentResult.getAmount() + result.getAmount());
                     yield currentResult;
                  } else {
                     yield result;
                  }
               } else {
                  yield super.getItem();
               }
            }
         };
      }

      @Override
      public void setItem(@Nullable ItemStack item) {
         if (ExprFurnaceSlot.this.selectedSlot == ExprFurnaceSlot.FurnaceSlot.OUTPUT && this.event instanceof FurnaceSmeltEvent furnaceSmeltEvent) {
            furnaceSmeltEvent.setResult(item != null ? item : new ItemStack(Material.AIR));
         } else if (ExprFurnaceSlot.this.getTime() == EventValues.TIME_FUTURE) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> access$001(this, item));
         } else {
            super.setItem(item);
         }
      }
   }

   private static enum FurnaceSlot {
      INPUT("(ore|input)", "input"),
      FUEL("fuel", "fuel"),
      OUTPUT("(result|output)", "output");

      private String pattern;
      private String toString;

      private FurnaceSlot(String pattern, String toString) {
         this.pattern = pattern;
         this.toString = toString;
      }
   }
}
