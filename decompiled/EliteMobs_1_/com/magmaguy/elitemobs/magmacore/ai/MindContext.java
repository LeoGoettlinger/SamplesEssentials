package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Objects;
import org.bukkit.entity.LivingEntity;

public record MindContext(
   LivingEntity entity, long gameTick, long generation, MindMemoryView memory, MindPerception perception, MindActuator actuator, MindActionRequester actions
) {
   public MindContext(
      LivingEntity entity, long gameTick, long generation, MindMemoryView memory, MindPerception perception, MindActuator actuator, MindActionRequester actions
   ) {
      Objects.requireNonNull(entity, "entity");
      Objects.requireNonNull(memory, "memory");
      Objects.requireNonNull(perception, "perception");
      Objects.requireNonNull(actuator, "actuator");
      Objects.requireNonNull(actions, "actions");
      this.entity = entity;
      this.gameTick = gameTick;
      this.generation = generation;
      this.memory = memory;
      this.perception = perception;
      this.actuator = actuator;
      this.actions = actions;
   }

   public MindContext(LivingEntity entity, long gameTick, long generation, MindMemoryView memory, MindPerception perception, MindActuator actuator) {
      this(entity, gameTick, generation, memory, perception, actuator, MindActionRequester.rejecting());
   }
}
