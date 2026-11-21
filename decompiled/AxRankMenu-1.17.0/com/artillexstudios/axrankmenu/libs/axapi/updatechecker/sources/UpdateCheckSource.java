package com.artillexstudios.axrankmenu.libs.axapi.updatechecker.sources;

import com.artillexstudios.axrankmenu.libs.axapi.updatechecker.ArtifactVersion;
import com.artillexstudios.axrankmenu.libs.axapi.updatechecker.UpdateCheck;

public interface UpdateCheckSource {
   UpdateCheck check(ArtifactVersion current);
}
