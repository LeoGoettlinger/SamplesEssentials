package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Nullable;

public class EvtMove extends SkriptEvent {
   private static final boolean HAS_ENTITY_MOVE = Skript.classExists("io.papermc.paper.event.entity.EntityMoveEvent");
   private EntityData<?> entityData;
   private boolean isPlayer;
   private boolean canBePlayer;
   private EvtMove.Move moveType;

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.entityData = (EntityData<?>)args[0].getSingle();
      this.isPlayer = Player.class.isAssignableFrom(this.entityData.getType());
      if (!HAS_ENTITY_MOVE && !this.isPlayer) {
         Skript.error("Entity move event requires Paper");
         return false;
      } else {
         this.canBePlayer = this.entityData.getType().isAssignableFrom(Player.class);
         if (matchedPattern > 0) {
            this.moveType = EvtMove.Move.MOVE_OR_ROTATE;
         } else if (parseResult.hasTag("rotate")) {
            this.moveType = EvtMove.Move.ROTATE;
         } else {
            this.moveType = EvtMove.Move.MOVE;
         }

         return true;
      }
   }

   @Override
   public boolean check(Event event) {
      Location from;
      Location to;
      if (this.canBePlayer && event instanceof PlayerMoveEvent playerMoveEvent) {
         from = playerMoveEvent.getFrom();
         to = playerMoveEvent.getTo();
      } else {
         if (!HAS_ENTITY_MOVE || !(event instanceof EntityMoveEvent entityMoveEvent)) {
            return false;
         }

         if (!this.entityData.isInstance(entityMoveEvent.getEntity())) {
            return false;
         }

         from = entityMoveEvent.getFrom();
         to = entityMoveEvent.getTo();
      }
      return switch (this.moveType) {
         case MOVE -> hasChangedPosition(from, to);
         case MOVE_OR_ROTATE -> true;
         case ROTATE -> hasChangedOrientation(from, to);
      };
   }

   @Override
   public Class<? extends Event>[] getEventClasses() {
      if (this.isPlayer) {
         return new Class[]{PlayerMoveEvent.class};
      } else if (HAS_ENTITY_MOVE) {
         return this.canBePlayer ? new Class[]{EntityMoveEvent.class, PlayerMoveEvent.class} : new Class[]{EntityMoveEvent.class};
      } else {
         throw new IllegalStateException("This event has not yet initialized!");
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.entityData + " " + this.moveType;
   }

   private static boolean hasChangedPosition(Location from, Location to) {
      return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ() || from.getWorld() != to.getWorld();
   }

   private static boolean hasChangedOrientation(Location from, Location to) {
      return from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
   }

   static {
      Class<? extends Event>[] events;
      if (HAS_ENTITY_MOVE) {
         events = CollectionUtils.array(PlayerMoveEvent.class, EntityMoveEvent.class);
      } else {
         events = CollectionUtils.array(PlayerMoveEvent.class);
      }

      Skript.registerEvent(
            "Move / Rotate",
            EvtMove.class,
            events,
            "%entitydata% (move|walk|step|rotate:(turn[ing] around|rotate))",
            "%entitydata% (move|walk|step) or (turn[ing] around|rotate)",
            "%entitydata% (turn[ing] around|rotate) or (move|walk|step)"
         )
         .description(
            "Called when a player or entity moves or rotates their head.",
            "NOTE: Move event will only be called when the entity/player moves position, keyword 'turn around' is for orientation (ie: looking around), and the combined syntax listens for both.",
            "NOTE: These events can be performance heavy as they are called quite often."
         )
         .examples(
            "on player move:",
            "\tif player does not have permission \"player.can.move\":",
            "\t\tcancel event",
            "on skeleton move:",
            "\tif event-entity is not in world \"world\":",
            "\t\tkill event-entity",
            "on player turning around:",
            "\tsend action bar \"You are currently turning your head around!\" to player"
         )
         .since("2.6, 2.8.0 (turn around)");
   }

   private static enum Move {
      MOVE("move"),
      MOVE_OR_ROTATE("move or rotate"),
      ROTATE("rotate");

      private final String name;

      private Move(String name) {
         this.name = name;
      }

      @Override
      public String toString() {
         return this.name;
      }
   }
}
