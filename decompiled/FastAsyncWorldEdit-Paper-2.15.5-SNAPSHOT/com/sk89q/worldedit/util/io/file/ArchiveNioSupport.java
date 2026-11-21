package com.sk89q.worldedit.util.io.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public interface ArchiveNioSupport {
   Optional<ArchiveDir> tryOpenAsDir(Path archive) throws IOException;
}
