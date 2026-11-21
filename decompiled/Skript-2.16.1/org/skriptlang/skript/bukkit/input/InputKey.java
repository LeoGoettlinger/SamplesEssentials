package org.skriptlang.skript.bukkit.input;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Input;

public enum InputKey {
   FORWARD,
   BACKWARD,
   RIGHT,
   LEFT,
   JUMP,
   SNEAK,
   SPRINT;

   public boolean check(Input input) {
      return switch (this) {
         case FORWARD -> input.isForward();
         case BACKWARD -> input.isBackward();
         case RIGHT -> input.isRight();
         case LEFT -> input.isLeft();
         case JUMP -> input.isJump();
         case SNEAK -> input.isSneak();
         case SPRINT -> input.isSprint();
      };
   }

   public static Set<InputKey> fromInput(Input input) {
      Set<InputKey> keys = EnumSet.noneOf(InputKey.class);

      for (InputKey key : values()) {
         if (key.check(input)) {
            keys.add(key);
         }
      }

      return keys;
   }
}
