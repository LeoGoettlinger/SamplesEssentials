package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.statement.StatementContext;

class InternetMapperFactory implements ColumnMapperFactory {
   private final IdentityHashMap<Class<?>, ColumnMapper<?>> mappers = new IdentityHashMap<>();

   InternetMapperFactory() {
      this.mappers.put(InetAddress.class, InternetMapperFactory::getInetAddress);
      this.mappers.put(URL.class, new GetterMapper<>(ResultSet::getURL));
      this.mappers.put(URI.class, new GetterMapper<>(InternetMapperFactory::getURI));
   }

   @Override
   public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
      Class<?> rawType = GenericTypes.getErasedType(type);
      return Optional.ofNullable(this.mappers.get(rawType));
   }

   private static URI getURI(ResultSet r, int i) throws SQLException {
      String s = r.getString(i);

      try {
         return s == null ? null : new URI(s);
      } catch (URISyntaxException var4) {
         throw new SQLException("Failed to convert data to URI", var4);
      }
   }

   private static InetAddress getInetAddress(ResultSet r, int i, StatementContext ctx) throws SQLException {
      String hostname = r.getString(i);

      try {
         return hostname == null ? null : InetAddress.getByName(hostname);
      } catch (UnknownHostException var5) {
         throw new MappingException("Could not map InetAddress", var5);
      }
   }
}
