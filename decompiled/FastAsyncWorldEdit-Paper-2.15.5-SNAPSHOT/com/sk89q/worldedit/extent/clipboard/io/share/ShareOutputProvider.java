package com.sk89q.worldedit.extent.clipboard.io.share;

import com.sk89q.worldedit.WorldEditException;
import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface ShareOutputProvider {
   void writeTo(OutputStream stream) throws IOException, WorldEditException;
}
