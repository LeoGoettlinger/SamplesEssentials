package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.Nullable;

@Name("Level")
@Description({"The experience level of a player."})
@Example.Examples({@Example("reduce the victim's level by 1"), @Example("set the player's level to 0"), @Example("on level change:\n\tset {_diff} to future xp level - past exp level\n\tbroadcast \"%player%'s level changed by %{_diff}%!\"\n")})
@Since({"unknown (before 2.1), 2.13.2 (allow player default)"})
@Events({"level change"})
public class ExprLevel extends SimplePropertyExpression<Player, Long> {
   protected Long[] get(Event event, Player[] source) {
      return super.get(
         source,
         player -> event instanceof PlayerLevelChangeEvent playerLevelChangeEvent && playerLevelChangeEvent.getPlayer() == player && !Delay.isDelayed(event)
            ? (long)(this.getTime() < 0 ? playerLevelChangeEvent.getOldLevel() : playerLevelChangeEvent.getNewLevel())
            : (long)player.getLevel()
      );
   }

   @Nullable
   public Long convert(Player player) {
      assert false;

      return null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (mode == Changer.ChangeMode.REMOVE_ALL) {
         return null;
      } else if (this.getParser().isCurrentEvent(PlayerRespawnEvent.class) && !this.getParser().getHasDelayBefore().isTrue()) {
         Skript.error("Cannot change a player's level in a respawn event. Add a delay of 1 tick or change the 'new level' in a death event.");
         return null;
      } else {
         if (this.getParser().isCurrentEvent(EntityDeathEvent.class)
            && this.getTime() == 0
            && this.getExpr().isDefault()
            && !this.getParser().getHasDelayBefore().isTrue()) {
            Skript.warning(
               "Changing the player's level in a death event will change the player's level before they die. Use either 'past level of player' or 'new level of player' to clearly state whether to change the level before or after they die."
            );
         }

         if (this.getTime() == -1 && !this.getParser().isCurrentEvent(EntityDeathEvent.class)) {
            return null;
         } else if (this.getTime() != 0 && this.getParser().isCurrentEvent(PlayerLevelChangeEvent.class)) {
            Skript.error("Changing the past or future level in a level change event has no effect.");
            return null;
         } else {
            return new Class[]{Number.class};
         }
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert mode != Changer.ChangeMode.REMOVE_ALL;

      int deltaAmount = delta == null ? 0 : ((Number)delta[0]).intValue();

      for (Player player : this.getExpr().getArray(event)) {
         int level;
         if (this.getTime() > 0 && event instanceof PlayerDeathEvent playerDeathEvent && playerDeathEvent.getEntity() == player && !Delay.isDelayed(event)) {
            level = playerDeathEvent.getNewLevel();
         } else {
            level = player.getLevel();
         }

         switch (mode) {
            case SET:
               level = deltaAmount;
               break;
            case ADD:
               level += deltaAmount;
               break;
            case REMOVE:
               level -= deltaAmount;
               break;
            case DELETE:
            case RESET:
               level = 0;
         }

         if (level < 0) {
            level = 0;
         }

         if (this.getTime() > 0 && event instanceof PlayerDeathEvent playerDeathEvent && playerDeathEvent.getEntity() == player && !Delay.isDelayed(event)) {
            playerDeathEvent.setNewLevel(level);
         } else {
            player.setLevel(level);
         }
      }
   }

   @Override
   public Class<Long> getReturnType() {
      return Long.class;
   }

   @Override
   public boolean setTime(int time) {
      return super.setTime(time, this.getExpr(), new Class[]{PlayerLevelChangeEvent.class, PlayerDeathEvent.class});
   }

   @Override
   protected String getPropertyName() {
      return "level";
   }

   static {
      registerDefault(ExprLevel.class, Long.class, "[xp|exp[erience]] level", "players");
   }
}
