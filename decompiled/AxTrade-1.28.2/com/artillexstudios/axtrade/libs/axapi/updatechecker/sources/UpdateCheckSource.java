package com.artillexstudios.axtrade.libs.axapi.updatechecker.sources;

import com.artillexstudios.axtrade.libs.axapi.updatechecker.ArtifactVersion;
import com.artillexstudios.axtrade.libs.axapi.updatechecker.UpdateCheck;

public interface UpdateCheckSource {
   UpdateCheck check(ArtifactVersion current);
}
