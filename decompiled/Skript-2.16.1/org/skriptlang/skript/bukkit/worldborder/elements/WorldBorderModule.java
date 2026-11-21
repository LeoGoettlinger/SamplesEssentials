package org.skriptlang.skript.bukkit.worldborder.elements;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderBoundsChangeFinishEvent;
import io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderEvent;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.bukkit.worldborder.elements.effects.EffWorldBorderExpand;
import org.skriptlang.skript.bukkit.worldborder.elements.expressions.ExprSecCreateWorldBorder;
import org.skriptlang.skript.bukkit.worldborder.elements.expressions.ExprWorldBorder;
import org.skriptlang.skript.bukkit.worldborder.elements.expressions.ExprWorldBorderCenter;
import org.skriptlang.skript.bukkit.worldborder.elements.expressions.ExprWorldBorderDamageAmount;
import org.skriptlang.skript.bukkit.worldborder.elements.expressions.ExprWorldBorderDamageBuffer;
import org.skriptlang.skript.bukkit.worldborder.elements.expressions.ExprWorldBorderSize;
import org.skriptlang.skript.bukkit.worldborder.elements.expressions.ExprWorldBorderWarningDistance;
import org.skriptlang.skript.bukkit.worldborder.elements.expressions.ExprWorldBorderWarningTime;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class WorldBorderModule extends HierarchicalAddonModule {
   public WorldBorderModule(AddonModule parentModule) {
      super(parentModule);
   }

   @Override
   protected void initSelf(SkriptAddon addon) {
      Classes.registerClass(
         new ClassInfo<>(WorldBorder.class, "worldborder")
            .user("world ?borders?")
            .name("World Border")
            .description("Represents the border of a world or player.")
            .since("2.11")
            .parser(new Parser<WorldBorder>() {
               @Override
               public boolean canParse(ParseContext context) {
                  return false;
               }

               public String toString(WorldBorder border, int flags) {
                  return border.getWorld() == null ? "virtual world border" : "world border of world named '" + border.getWorld().getName() + "'";
               }

               public String toVariableNameString(WorldBorder border) {
                  return this.toString(border, 0);
               }
            })
            .defaultExpression(new EventValueExpression<>(WorldBorder.class))
      );
   }

   @Override
   protected void loadSelf(SkriptAddon addon) {
      EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);
      SyntaxRegistry syntaxRegistry = this.moduleRegistry(addon);
      syntaxRegistry.register(
         BukkitSyntaxInfos.Event.KEY,
         ((BukkitSyntaxInfos.Event.Builder)BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "World Border Bounds Change")
               .addEvent(WorldBorderBoundsChangeEvent.class)
               .addPatterns(new String[]{"world[ ]border [bounds] chang(e|ing)"}))
            .addDescription(
               "Called when a world border changes its bounds, either over time, or instantly.", "This event does not get called for virtual borders."
            )
            .addExample(
               "on worldborder bounds change:\n\tbroadcast \"The diameter of %event-worldborder% is changing from %past event-number% to %event-number% over the next %event-timespan%\n"
            )
            .addSince("2.11")
            .build()
      );
      eventValueRegistry.register(EventValue.builder(WorldBorderBoundsChangeEvent.class, WorldBorder.class).getter(WorldBorderEvent::getWorldBorder).build());
      eventValueRegistry.register(EventValue.builder(WorldBorderBoundsChangeEvent.class, Number.class).getter(WorldBorderBoundsChangeEvent::getNewSize).build());
      eventValueRegistry.register(
         EventValue.builder(WorldBorderBoundsChangeEvent.class, Timespan.class).getter(event -> new Timespan(event.getDurationTicks())).build()
      );
      eventValueRegistry.register(
         EventValue.builder(WorldBorderBoundsChangeEvent.class, Number.class)
            .getter(WorldBorderBoundsChangeEvent::getOldSize)
            .time(EventValue.Time.PAST)
            .build()
      );
      syntaxRegistry.register(
         BukkitSyntaxInfos.Event.KEY,
         ((BukkitSyntaxInfos.Event.Builder)BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "World Border Bounds Finish Change")
               .addEvent(WorldBorderBoundsChangeFinishEvent.class)
               .addPatterns(new String[]{"world[ ]border [bounds] finish chang(e|ing)"}))
            .addDescription("Called when a moving world border has finished its move.\nThis event does not get called for virtual borders.\n")
            .addExample(
               "on worldborder bounds finish change:\n\tbroadcast \"Over the past %event-timespan%, the diameter of %event-worldborder% went from %past event-number% to %event-number%\"\n"
            )
            .addSince("2.11")
            .build()
      );
      eventValueRegistry.register(
         EventValue.builder(WorldBorderBoundsChangeFinishEvent.class, WorldBorder.class).getter(WorldBorderEvent::getWorldBorder).build()
      );
      eventValueRegistry.register(
         EventValue.builder(WorldBorderBoundsChangeFinishEvent.class, Number.class).getter(WorldBorderBoundsChangeFinishEvent::getNewSize).build()
      );
      eventValueRegistry.register(
         EventValue.builder(WorldBorderBoundsChangeFinishEvent.class, Number.class)
            .getter(WorldBorderBoundsChangeFinishEvent::getOldSize)
            .time(EventValue.Time.PAST)
            .build()
      );
      eventValueRegistry.register(
         EventValue.builder(WorldBorderBoundsChangeFinishEvent.class, Timespan.class).getter(event -> new Timespan((long)event.getDuration())).build()
      );
      syntaxRegistry.register(
         BukkitSyntaxInfos.Event.KEY,
         ((BukkitSyntaxInfos.Event.Builder)BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "World Border Center Change")
               .addEvent(WorldBorderCenterChangeEvent.class)
               .addPatterns(new String[]{"world[ ]border center chang(e|ing)"}))
            .addDescription("Called when a world border's center has changed.\nThis event does not get called for virtual borders.\n")
            .addExample(
               "on worldborder center change:\n\tbroadcast \"The center of %event-worldborder% has moved from %past event-location% to %event-location%\n"
            )
            .addSince("2.11")
            .build()
      );
      eventValueRegistry.register(EventValue.builder(WorldBorderCenterChangeEvent.class, WorldBorder.class).getter(WorldBorderEvent::getWorldBorder).build());
      eventValueRegistry.register(
         EventValue.builder(WorldBorderCenterChangeEvent.class, Location.class).getter(WorldBorderCenterChangeEvent::getNewCenter).build()
      );
      eventValueRegistry.register(
         EventValue.builder(WorldBorderCenterChangeEvent.class, Location.class)
            .getter(WorldBorderCenterChangeEvent::getOldCenter)
            .time(EventValue.Time.PAST)
            .build()
      );
      this.register(
         addon,
         new AddonModule.Registrar[]{
            a -> ExprSecCreateWorldBorder.register(a, eventValueRegistry),
            EffWorldBorderExpand::register,
            ExprWorldBorder::register,
            ExprWorldBorderCenter::register,
            ExprWorldBorderDamageAmount::register,
            ExprWorldBorderDamageBuffer::register,
            ExprWorldBorderSize::register,
            ExprWorldBorderWarningDistance::register,
            ExprWorldBorderWarningTime::register
         }
      );
   }

   @Override
   public String name() {
      return "worldborder";
   }
}
