package org.skriptlang.skript.bukkit.breeding.elements.events;

import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBreed extends SkriptEvent {
   @Nullable
   private Literal<EntityType> entitiesLiteral;
   @Nullable
   private EntityType[] entities;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         BukkitSyntaxInfos.Event.KEY,
         ((BukkitSyntaxInfos.Event.Builder)((BukkitSyntaxInfos.Event.Builder)BukkitSyntaxInfos.Event.builder(EvtBreed.class, "Entity Breed")
                  .addEvent(EntityBreedEvent.class)
                  .addPatterns(new String[]{"[entity] breed[ing] [of %-entitytypes%]"}))
               .addDescription("Called whenever two animals begin to conceive a child. The type can be specified.")
               .addExample(
                  "on breeding of llamas:\n\tsend \"When a %breeding mother% and %breeding father% love each other very much they make %offspring%\" to breeder\n"
               )
               .addSince("2.10")
               .supplier(EvtBreed::new))
            .build()
      );
      EventValues.registerEventValue(EntityBreedEvent.class, ItemStack.class, EntityBreedEvent::getBredWith);
   }

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      if (args[0] != null) {
         this.entitiesLiteral = (Literal<EntityType>)args[0];
         this.entities = this.entitiesLiteral.getAll();
      }

      return true;
   }

   @Override
   public boolean check(Event event) {
      return event instanceof EntityBreedEvent breedEvent && this.checkEntity(breedEvent.getEntity());
   }

   private boolean checkEntity(Entity entity) {
      if (this.entities != null) {
         for (EntityType entityType : this.entities) {
            if (entityType.isInstance(entity)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "on breeding" + (this.entitiesLiteral == null ? "" : " of " + this.entitiesLiteral);
   }
}
