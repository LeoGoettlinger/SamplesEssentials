package com.donutorders.libs.mysql.cj.protocol;

public interface Warning {
   int getLevel();

   long getCode();

   String getMessage();
}
