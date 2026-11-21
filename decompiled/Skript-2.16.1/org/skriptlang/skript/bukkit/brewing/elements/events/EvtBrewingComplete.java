package org.skriptlang.skript.bukkit.brewing.elements.events;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBrewingComplete extends SkriptEvent {
   @Nullable
   private Literal<?> types;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         BukkitSyntaxInfos.Event.KEY,
         ((BukkitSyntaxInfos.Event.Builder)((BukkitSyntaxInfos.Event.Builder)BukkitSyntaxInfos.Event.builder(EvtBrewingComplete.class, "Brewing Complete")
                  .addEvent(BrewEvent.class)
                  .addPatterns(new String[]{"brew[ing] [complet(e[d]|ion)|finish[ed]] [(of|for) %-itemtypes/potioneffecttypes%]"}))
               .addDescription("Called when a brewing stand finishes brewing an ingredient and changes the potions.")
               .addExample("on brew:\n\tbroadcast event-item\non brewing of speed potion:\non brew finished for speed 2 potion:\n")
               .addSince("2.13")
               .supplier(EvtBrewingComplete::new))
            .build()
      );
   }

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.types = args[0];
      return true;
   }

   @Override
   public boolean check(Event event) {
      if (!(event instanceof BrewEvent brewEvent)) {
         return false;
      } else if (this.types == null) {
         return true;
      } else {
         List<ItemStack> itemStacks = brewEvent.getResults();

         for (Object object : this.types.getArray()) {
            if (object instanceof ItemType itemType) {
               for (ItemStack itemStack : itemStacks) {
                  if (itemType.isOfType(itemStack)) {
                     return true;
                  }
               }
            } else if (object instanceof PotionEffectType potionEffectType) {
               for (ItemStack itemStackx : itemStacks) {
                  if (itemStackx.getItemMeta() instanceof PotionMeta potionMeta) {
                     for (PotionEffect potionEffect : potionMeta.getAllEffects()) {
                        if (potionEffect.getType() == potionEffectType) {
                           return true;
                        }
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append("brewing complete");
      if (this.types != null) {
         builder.append("for", this.types);
      }

      return builder.toString();
   }
}
