package com.earth2me.essentials.api;

import java.util.UUID;
import net.ess3.api.TranslatableException;

public class UserDoesNotExistException extends TranslatableException {
   public UserDoesNotExistException(final String name) {
      super("userDoesNotExist", name);
   }

   public UserDoesNotExistException(final UUID uuid) {
      super("uuidDoesNotExist", uuid.toString());
   }
}
