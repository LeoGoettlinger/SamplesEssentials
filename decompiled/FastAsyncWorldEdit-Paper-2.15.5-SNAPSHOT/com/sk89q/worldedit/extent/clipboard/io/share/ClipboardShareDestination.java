package com.sk89q.worldedit.extent.clipboard.io.share;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import java.util.Set;
import java.util.function.Consumer;

public interface ClipboardShareDestination {
   String getName();

   Set<String> getAliases();

   Consumer<Actor> share(ClipboardShareMetadata metadata, ShareOutputProvider serializer) throws Exception;

   ClipboardFormat getDefaultFormat();

   boolean supportsFormat(ClipboardFormat format);
}
