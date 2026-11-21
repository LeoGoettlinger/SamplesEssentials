package net.luckperms.api.util;

@FunctionalInterface
public interface Result {
   Result GENERIC_SUCCESS = () -> true;
   Result GENERIC_FAILURE = () -> false;

   boolean wasSuccessful();
}
