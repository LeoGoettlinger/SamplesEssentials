package com.sk89q.worldedit.session;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.auth.Subject;

public interface SessionOwner extends Subject {
   SessionKey getSessionKey();

   default LocalSession getSession() {
      return WorldEdit.getInstance().getSessionManager().get(this);
   }
}
