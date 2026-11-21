package de.oliver.cloud.brigadier;

import de.oliver.cloud.setting.Setting;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(
   status = Status.STABLE,
   since = "2.0.0"
)
public enum BrigadierSetting implements Setting {
   FORCE_EXECUTABLE;
}
