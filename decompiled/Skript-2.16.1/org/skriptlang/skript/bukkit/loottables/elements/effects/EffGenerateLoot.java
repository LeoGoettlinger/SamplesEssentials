package org.skriptlang.skript.bukkit.loottables.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Generate Loot")
@Description({"Generates the loot in the specified inventories from a loot table using a loot context. Not specifying a loot context will use a loot context with a location at the world's origin.", "Note that if the inventory is full, it will cause warnings in the console due to over-filling the inventory."})
@Example.Examples({@Example("generate loot of loot table \"minecraft:chests/simple_dungeon\" using loot context at player in {_inventory}"), @Example("generate loot using \"minecraft:chests/shipwreck_supply\" in {_inventory}")})
@Since({"2.10"})
public class EffGenerateLoot extends Effect {
   private Expression<LootTable> lootTable;
   private Expression<LootContext> context;
   private Expression<Inventory> inventories;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EFFECT,
         SyntaxInfo.builder(EffGenerateLoot.class)
            .addPatterns("generate [the] loot (of|using) %loottable% [(with|using) %-lootcontext%] in %inventories%")
            .supplier(EffGenerateLoot::new)
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.lootTable = (Expression<LootTable>)exprs[0];
      this.context = (Expression<LootContext>)exprs[1];
      this.inventories = (Expression<Inventory>)exprs[2];
      return true;
   }

   @Override
   protected void execute(Event event) {
      Random random = ThreadLocalRandom.current();
      LootContext context;
      if (this.context != null) {
         context = this.context.getSingle(event);
         if (context == null) {
            return;
         }
      } else {
         context = new LootContextWrapper(((World)Bukkit.getWorlds().get(0)).getSpawnLocation()).getContext();
      }

      LootTable table = this.lootTable.getSingle(event);
      if (table != null) {
         for (Inventory inventory : this.inventories.getArray(event)) {
            try {
               table.fillInventory(inventory, random, context);
            } catch (IllegalArgumentException var10) {
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append("generate loot using", this.lootTable);
      if (this.context != null) {
         builder.append("with", this.context);
      }

      builder.append("in", this.inventories);
      return builder.toString();
   }
}
