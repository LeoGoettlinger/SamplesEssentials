package com.sk89q.worldedit.internal.cui;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;

public interface CUIRegion {
   void describeCUI(LocalSession session, Actor player);

   void describeLegacyCUI(LocalSession session, Actor player);

   int getProtocolVersion();

   String getTypeID();

   String getLegacyTypeID();
}
