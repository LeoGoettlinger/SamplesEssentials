package su.nightexpress.nightcore.db.column;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ColumnDataReader<R> {
   ColumnDataReader<Boolean> BOOLEAN = ResultSet::getBoolean;
   ColumnDataReader<Integer> INTEGER = ResultSet::getInt;
   ColumnDataReader<Long> LONG = ResultSet::getLong;
   ColumnDataReader<Float> FLOAT = ResultSet::getFloat;
   ColumnDataReader<Double> DOUBLE = ResultSet::getDouble;
   ColumnDataReader<String> STRING = ResultSet::getString;
   ColumnDataReader<Timestamp> TIMESTAMP = ResultSet::getTimestamp;
   ColumnDataReader<UUID> UUID = (resultSet, column) -> {
      String raw = resultSet.getString(column);
      if (resultSet.wasNull()) {
         return null;
      } else {
         try {
            return java.util.UUID.fromString(raw);
         } catch (IllegalArgumentException var4) {
            return null;
         }
      }
   };

   @NonNull
   static <V> ColumnDataReader<V> jsonObject(@NonNull Gson gson, @NonNull Class<V> type) {
      return (resultSet, column) -> {
         String jsonString = resultSet.getString(column);
         return (V)(!resultSet.wasNull() && jsonString != null ? gson.fromJson(jsonString, type) : null);
      };
   }

   @NonNull
   static <K, V> ColumnDataReader<Map<K, V>> jsonMap(@NonNull Gson gson, @NonNull Class<K> keyType, @NonNull Class<V> valType) {
      return (resultSet, column) -> {
         String jsonString = resultSet.getString(column);
         if (!resultSet.wasNull() && jsonString != null) {
            Type mapType = TypeToken.getParameterized(Map.class, new Type[]{keyType, valType}).getType();
            return (Map<K, V>)gson.fromJson(jsonString, mapType);
         } else {
            return null;
         }
      };
   }

   @NonNull
   static <V> ColumnDataReader<List<V>> jsonList(@NonNull Gson gson, @NonNull Class<V> valType) {
      return (resultSet, column) -> {
         String jsonString = resultSet.getString(column);
         if (!resultSet.wasNull() && jsonString != null) {
            Type listType = TypeToken.getParameterized(List.class, new Type[]{valType}).getType();
            return (List<V>)gson.fromJson(jsonString, listType);
         } else {
            return null;
         }
      };
   }

   @NonNull
   static <V> ColumnDataReader<Set<V>> jsonSet(@NonNull Gson gson, @NonNull Class<V> valType) {
      return (resultSet, column) -> {
         String jsonString = resultSet.getString(column);
         if (!resultSet.wasNull() && jsonString != null) {
            Type setType = TypeToken.getParameterized(Set.class, new Type[]{valType}).getType();
            return (Set<V>)gson.fromJson(jsonString, setType);
         } else {
            return null;
         }
      };
   }

   @Nullable
   R readPrimitive(@NonNull ResultSet var1, @NonNull String var2) throws SQLException;

   @NonNull
   default Optional<R> read(@NonNull ResultSet resultSet, @NonNull String column) throws SQLException {
      R primitive = this.readPrimitive(resultSet, column);
      return primitive != null && !resultSet.wasNull() ? Optional.of(primitive) : Optional.empty();
   }
}
