package com.artillexstudios.axafkzone.libs.axapi.updatechecker.sources;

import com.artillexstudios.axafkzone.libs.axapi.updatechecker.ArtifactVersion;
import com.artillexstudios.axafkzone.libs.axapi.updatechecker.UpdateCheck;

public interface UpdateCheckSource {
   UpdateCheck check(ArtifactVersion current);
}
