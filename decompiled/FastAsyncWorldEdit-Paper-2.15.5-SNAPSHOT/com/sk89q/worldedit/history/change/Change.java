package com.sk89q.worldedit.history.change;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.history.UndoContext;

public interface Change {
   void undo(UndoContext context) throws WorldEditException;

   void redo(UndoContext context) throws WorldEditException;
}
