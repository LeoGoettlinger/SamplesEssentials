package org.jdbi.v3.core.config;

public interface JdbiConfig<This extends JdbiConfig<This>> {
   This createCopy();

   default void setRegistry(ConfigRegistry registry) {
   }
}
