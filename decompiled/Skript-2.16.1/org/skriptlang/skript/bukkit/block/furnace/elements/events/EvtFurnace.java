package org.skriptlang.skript.bukkit.block.furnace.elements.events;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtFurnace extends SkriptEvent {
   @Nullable
   private Literal<ItemType> types;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         BukkitSyntaxInfos.Event.KEY,
         ((BukkitSyntaxInfos.Event.Builder)((BukkitSyntaxInfos.Event.Builder)BukkitSyntaxInfos.Event.builder(EvtFurnace.class, "Smelt")
                  .addEvent(FurnaceSmeltEvent.class)
                  .addPatterns(new String[]{"[furnace] [ore] smelt[ed|ing] [of %-itemtypes%]", "[furnace] smelt[ed|ing] of ore"}))
               .addDescription("Called when a furnace smelts an item in its <a href='#ExprFurnaceSlot'>input slot</a>.")
               .addExample("on smelt:\n\tclear the smelted item\n")
               .addExample("on smelt of raw iron:\n\tbroadcast smelted item\n\tset the smelted item to iron block\n")
               .addSince("1.0, 2.10 (specific item)")
               .supplier(EvtFurnace::new))
            .build()
      );
      registry.register(
         BukkitSyntaxInfos.Event.KEY,
         ((BukkitSyntaxInfos.Event.Builder)((BukkitSyntaxInfos.Event.Builder)BukkitSyntaxInfos.Event.builder(EvtFurnace.class, "Fuel Burn")
                  .addEvent(FurnaceBurnEvent.class)
                  .addPatterns(new String[]{"[furnace] fuel burn[ing] [of %-itemtypes%]"}))
               .addDescription("Called when a furnace burns an item from its <a href='#ExprFurnaceSlot'>fuel slot</a>.")
               .addExample("on fuel burning:\n\tbroadcast fuel burned\n\tif burned fuel is coal:\n\t\tadd 20 seconds to burn time\n")
               .addSince("1.0, 2.10 (specific item)")
               .supplier(EvtFurnace::new))
            .build()
      );
      registry.register(
         BukkitSyntaxInfos.Event.KEY,
         ((BukkitSyntaxInfos.Event.Builder)((BukkitSyntaxInfos.Event.Builder)BukkitSyntaxInfos.Event.builder(EvtFurnace.class, "Furnace Item Extract")
                  .addEvent(FurnaceExtractEvent.class)
                  .addPatterns(new String[]{"furnace [item] extract[ion] [of %-itemtypes%]"}))
               .addDescription("Called when a player takes any item out of the furnace.")
               .addExample("on furnace extract:\n\tif event-items is an iron ingot:\n\t\tremove event-items from event-player's inventory\n")
               .addSince("2.10")
               .supplier(EvtFurnace::new))
            .build()
      );
      registry.register(
         BukkitSyntaxInfos.Event.KEY,
         ((BukkitSyntaxInfos.Event.Builder)((BukkitSyntaxInfos.Event.Builder)BukkitSyntaxInfos.Event.builder(EvtFurnace.class, "Start Smelt")
                  .addEvent(FurnaceStartSmeltEvent.class)
                  .addPatterns(new String[]{"[furnace] start [of] smelt[ing] [[of] %-itemtypes%]", "[furnace] smelt[ing] start [of %-itemtypes%]"}))
               .addDescription("Called when a furnace starts smelting an item in its ore slot.")
               .addExample("on smelting start:\n\tif the smelting item is raw iron:\n\t\tset total cook time to 1 second\n")
               .addExample("on smelting start of raw iron:\n\tadd 20 seconds to total cook time\n")
               .addSince("2.10")
               .supplier(EvtFurnace::new))
            .build()
      );
   }

   @Override
   public boolean init(Literal<?>[] exprs, int matchedPattern, SkriptParser.ParseResult parseResult) {
      if (exprs[0] != null) {
         this.types = (Literal<ItemType>)exprs[0];
      }

      return true;
   }

   @Override
   public boolean check(Event event) {
      if (this.types == null) {
         return true;
      } else {
         ItemType item;
         if (event instanceof FurnaceSmeltEvent smeltEvent) {
            item = new ItemType(smeltEvent.getSource());
         } else if (event instanceof FurnaceBurnEvent burnEvent) {
            item = new ItemType(burnEvent.getFuel());
         } else if (event instanceof FurnaceExtractEvent extractEvent) {
            item = new ItemType(extractEvent.getItemType());
         } else {
            if (!(event instanceof FurnaceStartSmeltEvent startEvent)) {
               assert false;

               return false;
            }

            item = new ItemType(startEvent.getSource());
         }

         return this.types.check(event, itemType -> itemType.isSupertypeOf(item));
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      Class<? extends Event> eventClass = this.getEventClasses()[0];
      String result;
      if (eventClass == FurnaceSmeltEvent.class) {
         result = "smelt";
      } else if (eventClass == FurnaceBurnEvent.class) {
         result = "burn";
      } else if (eventClass == FurnaceExtractEvent.class) {
         result = "extract";
      } else {
         if (eventClass != FurnaceStartSmeltEvent.class) {
            throw new IllegalStateException("Unexpected event: " + event);
         }

         result = "start smelt";
      }

      return result + " of " + Classes.toString(this.types);
   }
}
