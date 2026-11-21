package org.enginehub.piston.part;

public interface CommandFlag extends CommandPart {
   char getName();

   @Override
   default boolean isRequired() {
      return false;
   }
}
