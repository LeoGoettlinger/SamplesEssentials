package org.evenmorefish.fish.libs.hikaricp.hikari;

import org.evenmorefish.fish.libs.hikaricp.hikari.util.Credentials;

public interface HikariCredentialsProvider {
   Credentials getCredentials();
}
