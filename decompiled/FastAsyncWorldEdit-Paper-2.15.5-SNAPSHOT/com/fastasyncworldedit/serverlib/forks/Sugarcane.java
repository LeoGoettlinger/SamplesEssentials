package com.fastasyncworldedit.serverlib.forks;

import com.fastasyncworldedit.serverlib.CheckType;
import com.fastasyncworldedit.serverlib.ClassPresenceCheck;

public final class Sugarcane extends ClassPresenceCheck {
   private static final String SUGARCANE_CONFIG = "org.sugarcanemc.sugarcane.config.SugarcaneConfig";

   public Sugarcane() {
      super(CheckType.UNSAFE_FORK, "org.sugarcanemc.sugarcane.config.SugarcaneConfig");
   }

   @Override
   public String[] explain() {
      return new String[]{
         "You are running a server fork that is known to be extremely dangerous and lead to data loss",
         "due to a very dangerous patch history.",
         "",
         "It is strongly recommended you switch to a more stable,",
         "high-performing server software, like Paper or Tuinity.",
         "",
         "By continuing to use this fork you acknowledge you are running an unsupported server version.\""
      };
   }
}
