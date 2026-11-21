package com.sk89q.worldedit.session.storage;

import com.sk89q.worldedit.LocalSession;
import java.io.IOException;
import java.util.UUID;

public interface SessionStore {
   LocalSession load(UUID id) throws IOException;

   void save(UUID id, LocalSession session) throws IOException;
}
