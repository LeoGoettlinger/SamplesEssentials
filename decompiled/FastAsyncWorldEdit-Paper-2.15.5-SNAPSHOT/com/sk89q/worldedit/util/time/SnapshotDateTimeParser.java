package com.sk89q.worldedit.util.time;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import javax.annotation.Nullable;

public interface SnapshotDateTimeParser {
   @Nullable
   ZonedDateTime detectDateTime(Path path);
}
