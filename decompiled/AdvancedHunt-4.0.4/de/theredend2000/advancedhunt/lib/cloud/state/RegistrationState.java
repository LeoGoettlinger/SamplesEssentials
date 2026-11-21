package de.theredend2000.advancedhunt.lib.cloud.state;

import org.apiguardian.api.API;

@API(
   status = API.Status.STABLE
)
public enum RegistrationState implements State {
   BEFORE_REGISTRATION,
   REGISTERING,
   AFTER_REGISTRATION;
}
