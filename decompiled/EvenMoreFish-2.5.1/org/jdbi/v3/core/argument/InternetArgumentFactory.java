package org.jdbi.v3.core.argument;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.URI;
import java.net.URL;
import java.sql.PreparedStatement;

class InternetArgumentFactory extends DelegatingArgumentFactory {
   InternetArgumentFactory() {
      this.register(Inet4Address.class, 1111, (p, i, v) -> p.setString(i, v.getHostAddress()));
      this.register(Inet6Address.class, 1111, (p, i, v) -> p.setString(i, v.getHostAddress()));
      this.register(URL.class, 70, PreparedStatement::setURL);
      this.register(URI.class, 12, new ToStringBinder<>(PreparedStatement::setString));
   }
}
