package com.fastasyncworldedit.serverlib.forks;

import com.fastasyncworldedit.serverlib.CheckType;
import com.fastasyncworldedit.serverlib.ClassPresenceCheck;

public final class Akarin extends ClassPresenceCheck {
   private static final String AKARAIN_CONFIG = "io.akarin.server.Config";

   public Akarin() {
      super(CheckType.UNSAFE_FORK, "io.akarin.server.Config");
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
