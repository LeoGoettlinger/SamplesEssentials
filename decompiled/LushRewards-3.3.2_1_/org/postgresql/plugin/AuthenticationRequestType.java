package org.postgresql.plugin;

public enum AuthenticationRequestType {
   CLEARTEXT_PASSWORD,
   GSS,
   MD5_PASSWORD,
   SASL;
}
