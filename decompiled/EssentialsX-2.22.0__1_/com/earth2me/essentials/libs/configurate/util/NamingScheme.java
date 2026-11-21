package com.earth2me.essentials.libs.configurate.util;

@FunctionalInterface
public interface NamingScheme {
   String coerce(String input);
}
