package org.lushplugins.lushrewards.libraries.mysql.cj.protocol;

public interface Warning {
   int getLevel();

   long getCode();

   String getMessage();
}
