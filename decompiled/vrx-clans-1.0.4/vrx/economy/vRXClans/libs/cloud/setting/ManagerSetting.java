package vrx.economy.vRXClans.libs.cloud.setting;

import org.apiguardian.api.API;

@API(
   status = API.Status.STABLE
)
public enum ManagerSetting implements Setting {
   FORCE_SUGGESTION,
   @API(
      status = API.Status.STABLE
   )
   ALLOW_UNSAFE_REGISTRATION,
   @API(
      status = API.Status.STABLE
   )
   OVERRIDE_EXISTING_COMMANDS,
   @API(
      status = API.Status.EXPERIMENTAL
   )
   LIBERAL_FLAG_PARSING;
}
