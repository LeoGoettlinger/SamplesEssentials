package org.postgresql.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.BaseStatement;
import org.postgresql.core.ServerVersion;
import org.postgresql.core.TypeInfo;
import org.postgresql.util.GT;
import org.postgresql.util.PGobject;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class TypeInfoCache implements TypeInfo {
   private static final Logger LOGGER = Logger.getLogger(TypeInfoCache.class.getName());
   private final Map<String, Integer> pgNameToSQLType;
   private final Map<Integer, Integer> oidToSQLType;
   private final Map<String, String> pgNameToJavaClass;
   private final Map<Integer, String> oidToPgName;
   private final Map<String, Integer> pgNameToOid;
   private final Map<String, Integer> javaArrayTypeToOid;
   private final Map<String, Class<? extends PGobject>> pgNameToPgObject;
   private final Map<Integer, Integer> pgArrayToPgType;
   private final Map<Integer, Character> arrayOidToDelimiter;
   private final BaseConnection conn;
   private final int unknownLength;
   @Nullable
   private PreparedStatement getOidStatementSimple;
   @Nullable
   private PreparedStatement getOidStatementComplexNonArray;
   @Nullable
   private PreparedStatement getOidStatementComplexArray;
   @Nullable
   private PreparedStatement getNameStatement;
   @Nullable
   private PreparedStatement getArrayElementOidStatement;
   @Nullable
   private PreparedStatement getArrayDelimiterStatement;
   @Nullable
   private PreparedStatement getTypeInfoStatement;
   @Nullable
   private PreparedStatement getAllTypeInfoStatement;
   private final ResourceLock lock = new ResourceLock();
   private static final Object[][] types = new Object[][]{
      {"int2", 21, 5, "java.lang.Integer", 1005},
      {"int4", 23, 4, "java.lang.Integer", 1007},
      {"oid", 26, -5, "java.lang.Long", 1028},
      {"int8", 20, -5, "java.lang.Long", 1016},
      {"money", 790, 8, "java.lang.Double", 791},
      {"numeric", 1700, 2, "java.math.BigDecimal", 1231},
      {"float4", 700, 7, "java.lang.Float", 1021},
      {"float8", 701, 8, "java.lang.Double", 1022},
      {"char", 18, 1, "java.lang.String", 1002},
      {"bpchar", 1042, 1, "java.lang.String", 1014},
      {"varchar", 1043, 12, "java.lang.String", 1015},
      {"varbit", 1562, 1111, "java.lang.String", 1563},
      {"text", 25, 12, "java.lang.String", 1009},
      {"name", 19, 12, "java.lang.String", 1003},
      {"bytea", 17, -2, "[B", 1001},
      {"bool", 16, -7, "java.lang.Boolean", 1000},
      {"bit", 1560, -7, "java.lang.Boolean", 1561},
      {"date", 1082, 91, "java.sql.Date", 1182},
      {"time", 1083, 92, "java.sql.Time", 1183},
      {"timetz", 1266, 92, "java.sql.Time", 1270},
      {"timestamp", 1114, 93, "java.sql.Timestamp", 1115},
      {"timestamptz", 1184, 93, "java.sql.Timestamp", 1185},
      {"refcursor", 1790, 2012, "java.sql.ResultSet", 2201},
      {"json", 114, 1111, "org.postgresql.util.PGobject", 199},
      {"point", 600, 1111, "org.postgresql.geometric.PGpoint", 1017},
      {"box", 603, 1111, "org.postgresql.geometric.PGBox", 1020}
   };
   private static final ConcurrentMap<String, String> TYPE_ALIASES = new ConcurrentHashMap<>(30);

   public TypeInfoCache(BaseConnection conn, int unknownLength) {
      this.conn = conn;
      this.unknownLength = unknownLength;
      this.oidToPgName = new HashMap<>((int)Math.round(types.length * 1.5));
      this.pgNameToOid = new HashMap<>((int)Math.round(types.length * 1.5));
      this.javaArrayTypeToOid = new HashMap<>((int)Math.round(types.length * 1.5));
      this.pgNameToJavaClass = new HashMap<>((int)Math.round(types.length * 1.5));
      this.pgNameToPgObject = new HashMap<>((int)Math.round(types.length * 1.5));
      this.pgArrayToPgType = new HashMap<>((int)Math.round(types.length * 1.5));
      this.arrayOidToDelimiter = new HashMap<>((int)Math.round(types.length * 2.5));
      this.pgNameToSQLType = Collections.synchronizedMap(new HashMap<>((int)Math.round(types.length * 1.5)));
      this.oidToSQLType = Collections.synchronizedMap(new HashMap<>((int)Math.round(types.length * 1.5)));

      for (Object[] type : types) {
         String pgTypeName = (String)type[0];
         Integer oid = (Integer)type[1];
         Integer sqlType = (Integer)type[2];
         String javaClass = (String)type[3];
         Integer arrayOid = (Integer)type[4];
         this.addCoreType(pgTypeName, oid, sqlType, javaClass, arrayOid);
      }

      this.pgNameToJavaClass.put("hstore", Map.class.getName());
   }

   @Override
   public void addCoreType(String pgTypeName, Integer oid, Integer sqlType, String javaClass, Integer arrayOid) {
      ResourceLock ignore = this.lock.obtain();

      try {
         this.pgNameToJavaClass.put(pgTypeName, javaClass);
         this.pgNameToOid.put(pgTypeName, oid);
         this.oidToPgName.put(oid, pgTypeName);
         this.javaArrayTypeToOid.put(javaClass, arrayOid);
         this.pgArrayToPgType.put(arrayOid, oid);
         this.pgNameToSQLType.put(pgTypeName, sqlType);
         this.oidToSQLType.put(oid, sqlType);
         Character delim = ',';
         if ("box".equals(pgTypeName)) {
            delim = ';';
         }

         this.arrayOidToDelimiter.put(oid, delim);
         this.arrayOidToDelimiter.put(arrayOid, delim);
         String pgArrayTypeName = pgTypeName + "[]";
         this.pgNameToJavaClass.put(pgArrayTypeName, "java.sql.Array");
         this.pgNameToSQLType.put(pgArrayTypeName, 2003);
         this.oidToSQLType.put(arrayOid, 2003);
         this.pgNameToOid.put(pgArrayTypeName, arrayOid);
         pgArrayTypeName = "_" + pgTypeName;
         if (!this.pgNameToJavaClass.containsKey(pgArrayTypeName)) {
            this.pgNameToJavaClass.put(pgArrayTypeName, "java.sql.Array");
            this.pgNameToSQLType.put(pgArrayTypeName, 2003);
            this.pgNameToOid.put(pgArrayTypeName, arrayOid);
            this.oidToPgName.put(arrayOid, pgArrayTypeName);
         }
      } catch (Throwable var10) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (ignore != null) {
         ignore.close();
      }
   }

   @Override
   public void addDataType(String type, Class<? extends PGobject> klass) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      try {
         this.pgNameToPgObject.put(type, klass);
         this.pgNameToJavaClass.put(type, klass.getName());
      } catch (Throwable var7) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (ignore != null) {
         ignore.close();
      }
   }

   @Override
   public Iterator<String> getPGTypeNamesWithSQLTypes() {
      return this.pgNameToSQLType.keySet().iterator();
   }

   @Override
   public Iterator<Integer> getPGTypeOidsWithSQLTypes() {
      return this.oidToSQLType.keySet().iterator();
   }

   private String getSQLTypeQuery(boolean typoidParam) {
      StringBuilder sql = new StringBuilder();
      sql.append("SELECT typinput='pg_catalog.array_in'::regproc as is_array, typtype, typname, pg_type.oid ");
      sql.append("  FROM pg_catalog.pg_type ");
      sql.append("  LEFT JOIN (select ns.oid as nspoid, ns.nspname, r.r ");
      sql.append("          from pg_namespace as ns ");
      sql.append("          join ( select s.r, (current_schemas(false))[s.r] as nspname ");
      sql.append("                   from generate_series(1, array_upper(current_schemas(false), 1)) as s(r) ) as r ");
      sql.append("         using ( nspname ) ");
      sql.append("       ) as sp ");
      sql.append("    ON sp.nspoid = typnamespace ");
      if (typoidParam) {
         sql.append(" WHERE pg_type.oid = ? ");
      }

      sql.append(" ORDER BY sp.r, pg_type.oid DESC;");
      return sql.toString();
   }

   private int getSQLTypeFromQueryResult(ResultSet rs) throws SQLException {
      Integer type = null;
      boolean isArray = rs.getBoolean("is_array");
      String typtype = rs.getString("typtype");
      if (isArray) {
         type = 2003;
      } else if ("c".equals(typtype)) {
         type = 2002;
      } else if ("d".equals(typtype)) {
         type = 2001;
      } else if ("e".equals(typtype)) {
         type = 12;
      }

      if (type == null) {
         type = 1111;
      }

      return type;
   }

   private PreparedStatement prepareGetAllTypeInfoStatement() throws SQLException {
      PreparedStatement getAllTypeInfoStatement = this.getAllTypeInfoStatement;
      if (getAllTypeInfoStatement == null) {
         getAllTypeInfoStatement = this.conn.prepareStatement(this.getSQLTypeQuery(false));
         this.getAllTypeInfoStatement = getAllTypeInfoStatement;
      }

      return getAllTypeInfoStatement;
   }

   public void cacheSQLTypes() throws SQLException {
      LOGGER.log(Level.FINEST, "caching all SQL typecodes");
      PreparedStatement getAllTypeInfoStatement = this.prepareGetAllTypeInfoStatement();
      if (!((BaseStatement)getAllTypeInfoStatement).executeWithFlags(16)) {
         throw new PSQLException(GT.tr("No results were returned by the query."), PSQLState.NO_DATA);
      } else {
         ResultSet rs = Nullness.castNonNull(getAllTypeInfoStatement.getResultSet());

         while (rs.next()) {
            String typeName = Nullness.castNonNull(rs.getString("typname"));
            Integer type = this.getSQLTypeFromQueryResult(rs);
            if (!this.pgNameToSQLType.containsKey(typeName)) {
               this.pgNameToSQLType.put(typeName, type);
            }

            Integer typeOid = this.longOidToInt(Nullness.castNonNull(rs.getLong("oid")));
            if (!this.oidToSQLType.containsKey(typeOid)) {
               this.oidToSQLType.put(typeOid, type);
            }
         }

         rs.close();
      }
   }

   private PreparedStatement prepareGetTypeInfoStatement() throws SQLException {
      PreparedStatement getTypeInfoStatement = this.getTypeInfoStatement;
      if (getTypeInfoStatement == null) {
         getTypeInfoStatement = this.conn.prepareStatement(this.getSQLTypeQuery(true));
         this.getTypeInfoStatement = getTypeInfoStatement;
      }

      return getTypeInfoStatement;
   }

   @Override
   public int getSQLType(String pgTypeName) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      short var8;
      label55: {
         int var9;
         label56: {
            try {
               if (pgTypeName.endsWith("[]")) {
                  var8 = 2003;
                  break label55;
               }

               Integer i = this.pgNameToSQLType.get(pgTypeName);
               if (i != null) {
                  var9 = i;
                  break label56;
               }

               Integer var7 = this.getSQLType(Nullness.castNonNull(this.getPGType(pgTypeName)));
               this.pgNameToSQLType.put(pgTypeName, var7);
               var9 = var7;
            } catch (Throwable var6) {
               if (ignore != null) {
                  try {
                     ignore.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (ignore != null) {
               ignore.close();
            }

            return var9;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var9;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var8;
   }

   @Override
   public int getJavaArrayType(String className) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      int var7;
      label43: {
         try {
            Integer oid = this.javaArrayTypeToOid.get(className);
            if (oid == null) {
               var7 = 0;
               break label43;
            }

            var7 = oid;
         } catch (Throwable var6) {
            if (ignore != null) {
               try {
                  ignore.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var7;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var7;
   }

   @Override
   public int getSQLType(int typeOid) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      short var10;
      label64: {
         int var11;
         label65: {
            int var7;
            try {
               if (typeOid == 0) {
                  var10 = 1111;
                  break label64;
               }

               Integer i = this.oidToSQLType.get(typeOid);
               if (i != null) {
                  var11 = i;
                  break label65;
               }

               LOGGER.log(Level.FINEST, "querying SQL typecode for pg type oid ''{0}''", this.intOidToLong(typeOid));
               PreparedStatement getTypeInfoStatement = this.prepareGetTypeInfoStatement();
               getTypeInfoStatement.setLong(1, this.intOidToLong(typeOid));
               if (!((BaseStatement)getTypeInfoStatement).executeWithFlags(16)) {
                  throw new PSQLException(GT.tr("No results were returned by the query."), PSQLState.NO_DATA);
               }

               ResultSet rs = Nullness.castNonNull(getTypeInfoStatement.getResultSet());
               int sqlType = 1111;
               if (rs.next()) {
                  sqlType = this.getSQLTypeFromQueryResult(rs);
               }

               rs.close();
               this.oidToSQLType.put(typeOid, sqlType);
               var7 = sqlType;
            } catch (Throwable var9) {
               if (ignore != null) {
                  try {
                     ignore.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (ignore != null) {
               ignore.close();
            }

            return var7;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var11;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var10;
   }

   private PreparedStatement getOidStatement(String pgTypeName) throws SQLException {
      boolean isArray = pgTypeName.endsWith("[]");
      boolean hasQuote = pgTypeName.contains("\"");
      int dotIndex = pgTypeName.indexOf(46);
      if (dotIndex == -1 && !hasQuote && !isArray) {
         PreparedStatement getOidStatementSimple = this.getOidStatementSimple;
         if (getOidStatementSimple == null) {
            String sql = "SELECT pg_type.oid, typname   FROM pg_catalog.pg_type   LEFT   JOIN (select ns.oid as nspoid, ns.nspname, r.r           from pg_namespace as ns           join ( select s.r, (current_schemas(false))[s.r] as nspname                    from generate_series(1, array_upper(current_schemas(false), 1)) as s(r) ) as r          using ( nspname )        ) as sp     ON sp.nspoid = typnamespace  WHERE typname = ?  ORDER BY sp.r, pg_type.oid DESC LIMIT 1;";
            this.getOidStatementSimple = getOidStatementSimple = this.conn.prepareStatement(sql);
         }

         String lcName = pgTypeName.toLowerCase(Locale.ROOT);
         getOidStatementSimple.setString(1, lcName);
         return getOidStatementSimple;
      } else {
         PreparedStatement oidStatementComplex;
         if (isArray) {
            PreparedStatement getOidStatementComplexArray = this.getOidStatementComplexArray;
            if (getOidStatementComplexArray == null) {
               String sql;
               if (this.conn.haveMinimumServerVersion(ServerVersion.v8_3)) {
                  sql = "SELECT t.typarray, arr.typname   FROM pg_catalog.pg_type t  JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid  JOIN pg_catalog.pg_type arr ON arr.oid = t.typarray WHERE t.typname = ? AND (n.nspname = ? OR ? AND n.nspname = ANY (current_schemas(true))) ORDER BY t.oid DESC LIMIT 1";
               } else {
                  sql = "SELECT t.oid, t.typname   FROM pg_catalog.pg_type t  JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid WHERE t.typelem = (SELECT oid FROM pg_catalog.pg_type WHERE typname = ?) AND substring(t.typname, 1, 1) = '_' AND t.typlen = -1 AND (n.nspname = ? OR ? AND n.nspname = ANY (current_schemas(true))) ORDER BY t.typelem DESC LIMIT 1";
               }

               this.getOidStatementComplexArray = getOidStatementComplexArray = this.conn.prepareStatement(sql);
            }

            oidStatementComplex = getOidStatementComplexArray;
         } else {
            PreparedStatement getOidStatementComplexNonArray = this.getOidStatementComplexNonArray;
            if (getOidStatementComplexNonArray == null) {
               String sql = "SELECT t.oid, t.typname   FROM pg_catalog.pg_type t  JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid WHERE t.typname = ? AND (n.nspname = ? OR ? AND n.nspname = ANY (current_schemas(true))) ORDER BY t.oid DESC LIMIT 1";
               this.getOidStatementComplexNonArray = getOidStatementComplexNonArray = this.conn.prepareStatement(sql);
            }

            oidStatementComplex = getOidStatementComplexNonArray;
         }

         String fullName = isArray ? pgTypeName.substring(0, pgTypeName.length() - 2) : pgTypeName;
         String name;
         String schema;
         if (dotIndex == -1) {
            schema = null;
            name = fullName;
         } else if (fullName.startsWith("\"")) {
            if (fullName.endsWith("\"")) {
               String[] parts = fullName.split("\"\\.\"");
               schema = parts.length == 2 ? parts[0] + "\"" : null;
               name = parts.length == 2 ? "\"" + parts[1] : parts[0];
            } else {
               int lastDotIndex = fullName.lastIndexOf(46);
               name = fullName.substring(lastDotIndex + 1);
               schema = fullName.substring(0, lastDotIndex);
            }
         } else {
            schema = fullName.substring(0, dotIndex);
            name = fullName.substring(dotIndex + 1);
         }

         if (schema != null && schema.startsWith("\"") && schema.endsWith("\"")) {
            schema = schema.substring(1, schema.length() - 1);
         } else if (schema != null) {
            schema = schema.toLowerCase(Locale.ROOT);
         }

         if (name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
         } else {
            name = name.toLowerCase(Locale.ROOT);
         }

         oidStatementComplex.setString(1, name);
         oidStatementComplex.setString(2, schema);
         oidStatementComplex.setBoolean(3, schema == null);
         return oidStatementComplex;
      }
   }

   @Override
   public int getPGType(String pgTypeName) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      byte var10;
      label64: {
         int var11;
         label65: {
            int var12;
            try {
               if (pgTypeName == null) {
                  var10 = 0;
                  break label64;
               }

               Integer oid = this.pgNameToOid.get(pgTypeName);
               if (oid != null) {
                  var11 = oid;
                  break label65;
               }

               PreparedStatement oidStatement = this.getOidStatement(pgTypeName);
               if (!((BaseStatement)oidStatement).executeWithFlags(16)) {
                  throw new PSQLException(GT.tr("No results were returned by the query."), PSQLState.NO_DATA);
               }

               Integer var9 = 0;
               ResultSet rs = Nullness.castNonNull(oidStatement.getResultSet());
               if (rs.next()) {
                  var9 = (int)rs.getLong(1);
                  String internalName = Nullness.castNonNull(rs.getString(2));
                  this.oidToPgName.put(var9, internalName);
                  this.pgNameToOid.put(internalName, var9);
               }

               this.pgNameToOid.put(pgTypeName, var9);
               rs.close();
               var12 = var9;
            } catch (Throwable var8) {
               if (ignore != null) {
                  try {
                     ignore.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (ignore != null) {
               ignore.close();
            }

            return var12;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var11;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var10;
   }

   @Nullable
   @Override
   public String getPGType(int oid) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      String pgTypeName;
      label77: {
         String var12;
         label78: {
            String var13;
            try {
               if (oid == 0) {
                  pgTypeName = null;
                  break label77;
               }

               pgTypeName = this.oidToPgName.get(oid);
               if (pgTypeName != null) {
                  var12 = pgTypeName;
                  break label78;
               }

               PreparedStatement getNameStatement = this.prepareGetNameStatement();
               getNameStatement.setInt(1, oid);
               if (!((BaseStatement)getNameStatement).executeWithFlags(16)) {
                  throw new PSQLException(GT.tr("No results were returned by the query."), PSQLState.NO_DATA);
               }

               ResultSet rs = Nullness.castNonNull(getNameStatement.getResultSet());
               if (rs.next()) {
                  boolean onPath = rs.getBoolean(1);
                  String schema = Nullness.castNonNull(rs.getString(2), "schema");
                  String name = Nullness.castNonNull(rs.getString(3), "name");
                  if (onPath) {
                     pgTypeName = name;
                     this.pgNameToOid.put(schema + "." + name, oid);
                  } else {
                     pgTypeName = "\"" + schema + "\".\"" + name + "\"";
                     if (schema.equals(schema.toLowerCase(Locale.ROOT))
                        && schema.indexOf(46) == -1
                        && name.equals(name.toLowerCase(Locale.ROOT))
                        && name.indexOf(46) == -1) {
                        this.pgNameToOid.put(schema + "." + name, oid);
                     }
                  }

                  this.pgNameToOid.put(pgTypeName, oid);
                  this.oidToPgName.put(oid, pgTypeName);
               }

               rs.close();
               var13 = pgTypeName;
            } catch (Throwable var10) {
               if (ignore != null) {
                  try {
                     ignore.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (ignore != null) {
               ignore.close();
            }

            return var13;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var12;
      }

      if (ignore != null) {
         ignore.close();
      }

      return pgTypeName;
   }

   private PreparedStatement prepareGetNameStatement() throws SQLException {
      PreparedStatement getNameStatement = this.getNameStatement;
      if (getNameStatement == null) {
         String sql = "SELECT n.nspname = ANY(current_schemas(true)), n.nspname, t.typname FROM pg_catalog.pg_type t JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid WHERE t.oid = ?";
         this.getNameStatement = getNameStatement = this.conn.prepareStatement(sql);
      }

      return getNameStatement;
   }

   @Override
   public int getPGArrayType(@Nullable String elementTypeName) throws SQLException {
      elementTypeName = this.getTypeForAlias(elementTypeName);
      return this.getPGType(elementTypeName + "[]");
   }

   protected int convertArrayToBaseOid(int oid) {
      ResourceLock ignore = this.lock.obtain();

      int var7;
      label43: {
         try {
            Integer i = this.pgArrayToPgType.get(oid);
            if (i == null) {
               var7 = oid;
               break label43;
            }

            var7 = i;
         } catch (Throwable var6) {
            if (ignore != null) {
               try {
                  ignore.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var7;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var7;
   }

   @Override
   public char getArrayDelimiter(int oid) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      char var11;
      label64: {
         char var12;
         label65: {
            char var7;
            try {
               if (oid == 0) {
                  var11 = ',';
                  break label64;
               }

               Character delim = this.arrayOidToDelimiter.get(oid);
               if (delim != null) {
                  var12 = delim;
                  break label65;
               }

               PreparedStatement getArrayDelimiterStatement = this.prepareGetArrayDelimiterStatement();
               getArrayDelimiterStatement.setInt(1, oid);
               if (!((BaseStatement)getArrayDelimiterStatement).executeWithFlags(16)) {
                  throw new PSQLException(GT.tr("No results were returned by the query."), PSQLState.NO_DATA);
               }

               ResultSet rs = Nullness.castNonNull(getArrayDelimiterStatement.getResultSet());
               if (!rs.next()) {
                  throw new PSQLException(GT.tr("No results were returned by the query."), PSQLState.NO_DATA);
               }

               String s = Nullness.castNonNull(rs.getString(1));
               delim = s.charAt(0);
               this.arrayOidToDelimiter.put(oid, delim);
               rs.close();
               var7 = delim;
            } catch (Throwable var9) {
               if (ignore != null) {
                  try {
                     ignore.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (ignore != null) {
               ignore.close();
            }

            return var7;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var12;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var11;
   }

   private PreparedStatement prepareGetArrayDelimiterStatement() throws SQLException {
      PreparedStatement getArrayDelimiterStatement = this.getArrayDelimiterStatement;
      if (getArrayDelimiterStatement == null) {
         String sql = "SELECT e.typdelim FROM pg_catalog.pg_type t, pg_catalog.pg_type e WHERE t.oid = ? and t.typelem = e.oid";
         this.getArrayDelimiterStatement = getArrayDelimiterStatement = this.conn.prepareStatement(sql);
      }

      return getArrayDelimiterStatement;
   }

   @Override
   public int getPGArrayElement(int oid) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      byte var14;
      label71: {
         int var15;
         label72: {
            int var10;
            try {
               if (oid == 0) {
                  var14 = 0;
                  break label71;
               }

               Integer pgType = this.pgArrayToPgType.get(oid);
               if (pgType != null) {
                  var15 = pgType;
                  break label72;
               }

               PreparedStatement getArrayElementOidStatement = this.prepareGetArrayElementOidStatement();
               getArrayElementOidStatement.setInt(1, oid);
               if (!((BaseStatement)getArrayElementOidStatement).executeWithFlags(16)) {
                  throw new PSQLException(GT.tr("No results were returned by the query."), PSQLState.NO_DATA);
               }

               ResultSet rs = Nullness.castNonNull(getArrayElementOidStatement.getResultSet());
               if (!rs.next()) {
                  throw new PSQLException(GT.tr("No results were returned by the query."), PSQLState.NO_DATA);
               }

               Integer var13 = (int)rs.getLong(1);
               boolean onPath = rs.getBoolean(2);
               String schema = rs.getString(3);
               String name = Nullness.castNonNull(rs.getString(4));
               this.pgArrayToPgType.put(oid, var13);
               this.pgNameToOid.put(schema + "." + name, var13);
               String fullName = "\"" + schema + "\".\"" + name + "\"";
               this.pgNameToOid.put(fullName, var13);
               if (onPath && name.equals(name.toLowerCase(Locale.ROOT))) {
                  this.oidToPgName.put(var13, name);
                  this.pgNameToOid.put(name, var13);
               } else {
                  this.oidToPgName.put(var13, fullName);
               }

               rs.close();
               var10 = var13;
            } catch (Throwable var12) {
               if (ignore != null) {
                  try {
                     ignore.close();
                  } catch (Throwable var11) {
                     var12.addSuppressed(var11);
                  }
               }

               throw var12;
            }

            if (ignore != null) {
               ignore.close();
            }

            return var10;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var15;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var14;
   }

   private PreparedStatement prepareGetArrayElementOidStatement() throws SQLException {
      PreparedStatement getArrayElementOidStatement = this.getArrayElementOidStatement;
      if (getArrayElementOidStatement == null) {
         String sql = "SELECT e.oid, n.nspname = ANY(current_schemas(true)), n.nspname, e.typname FROM pg_catalog.pg_type t JOIN pg_catalog.pg_type e ON t.typelem = e.oid JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid WHERE t.oid = ?";
         this.getArrayElementOidStatement = getArrayElementOidStatement = this.conn.prepareStatement(sql);
      }

      return getArrayElementOidStatement;
   }

   @Nullable
   @Override
   public Class<? extends PGobject> getPGobject(String type) {
      ResourceLock ignore = this.lock.obtain();

      Class var3;
      try {
         var3 = this.pgNameToPgObject.get(type);
      } catch (Throwable var6) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var3;
   }

   @Override
   public String getJavaClass(int oid) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      String result;
      label64: {
         String var9;
         label65: {
            try {
               String pgTypeName = this.getPGType(oid);
               if (pgTypeName == null) {
                  result = "java.lang.String";
                  break label64;
               }

               result = this.pgNameToJavaClass.get(pgTypeName);
               if (result != null) {
                  var9 = result;
                  break label65;
               }

               if (this.getSQLType(pgTypeName) == 2003) {
                  result = "java.sql.Array";
                  this.pgNameToJavaClass.put(pgTypeName, result);
               }

               var9 = result == null ? "java.lang.String" : result;
            } catch (Throwable var7) {
               if (ignore != null) {
                  try {
                     ignore.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (ignore != null) {
               ignore.close();
            }

            return var9;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var9;
      }

      if (ignore != null) {
         ignore.close();
      }

      return result;
   }

   @Nullable
   @Override
   public String getTypeForAlias(@Nullable String alias) {
      if (alias == null) {
         return null;
      } else {
         String type = TYPE_ALIASES.get(alias);
         if (type != null) {
            return type;
         } else {
            type = TYPE_ALIASES.get(alias.toLowerCase(Locale.ROOT));
            if (type == null) {
               type = alias;
            }

            TYPE_ALIASES.put(alias, type);
            return type;
         }
      }
   }

   @Override
   public int getPrecision(int oid, int typmod) {
      oid = this.convertArrayToBaseOid(oid);
      switch (oid) {
         case 16:
         case 18:
            return 1;
         case 17:
         case 25:
         default:
            return this.unknownLength;
         case 20:
            return 19;
         case 21:
            return 5;
         case 23:
         case 26:
            return 10;
         case 700:
            return 8;
         case 701:
            return 17;
         case 1042:
         case 1043:
            if (typmod == -1) {
               return this.unknownLength;
            }

            return typmod - 4;
         case 1082:
         case 1083:
         case 1114:
         case 1184:
         case 1186:
         case 1266:
            return this.getDisplaySize(oid, typmod);
         case 1560:
            return typmod;
         case 1562:
            if (typmod == -1) {
               return this.unknownLength;
            }

            return typmod;
         case 1700:
            return typmod == -1 ? 0 : (typmod - 4 & -65536) >> 16;
      }
   }

   @Override
   public int getScale(int oid, int typmod) {
      oid = this.convertArrayToBaseOid(oid);
      switch (oid) {
         case 700:
            return 8;
         case 701:
            return 17;
         case 1083:
         case 1114:
         case 1184:
         case 1266:
            if (typmod == -1) {
               return 6;
            }

            return typmod;
         case 1186:
            if (typmod == -1) {
               return 6;
            }

            return typmod & 65535;
         case 1700:
            if (typmod == -1) {
               return 0;
            }

            return typmod - 4 & 65535;
         default:
            return 0;
      }
   }

   @Override
   public boolean isCaseSensitive(int oid) {
      oid = this.convertArrayToBaseOid(oid);
      switch (oid) {
         case 16:
         case 20:
         case 21:
         case 23:
         case 26:
         case 700:
         case 701:
         case 1082:
         case 1083:
         case 1114:
         case 1184:
         case 1186:
         case 1266:
         case 1560:
         case 1562:
         case 1700:
            return false;
         default:
            return true;
      }
   }

   @Override
   public boolean isSigned(int oid) {
      oid = this.convertArrayToBaseOid(oid);
      switch (oid) {
         case 20:
         case 21:
         case 23:
         case 700:
         case 701:
         case 1700:
            return true;
         default:
            return false;
      }
   }

   @Override
   public int getDisplaySize(int oid, int typmod) {
      oid = this.convertArrayToBaseOid(oid);
      switch (oid) {
         case 16:
            return 1;
         case 17:
         case 25:
            return this.unknownLength;
         case 18:
            return 1;
         case 20:
            return 20;
         case 21:
            return 6;
         case 23:
            return 11;
         case 26:
            return 10;
         case 700:
            return 15;
         case 701:
            return 25;
         case 1042:
         case 1043:
            if (typmod == -1) {
               return this.unknownLength;
            }

            return typmod - 4;
         case 1082:
            return 13;
         case 1083:
         case 1114:
         case 1184:
         case 1266:
            int secondSize;
            switch (typmod) {
               case -1:
                  secondSize = 7;
                  break;
               case 0:
                  secondSize = 0;
                  break;
               case 1:
                  secondSize = 3;
                  break;
               default:
                  secondSize = typmod + 1;
            }

            switch (oid) {
               case 1083:
                  return 8 + secondSize;
               case 1114:
                  return 22 + secondSize;
               case 1184:
                  return 22 + secondSize + 6;
               case 1266:
                  return 8 + secondSize + 6;
            }
         case 1186:
            return 49;
         case 1560:
            return typmod;
         case 1562:
            if (typmod == -1) {
               return this.unknownLength;
            }

            return typmod;
         case 1700:
            if (typmod == -1) {
               return 131089;
            }

            int precision = typmod - 4 >> 16 & 65535;
            int scale = typmod - 4 & 65535;
            return 1 + precision + (scale != 0 ? 1 : 0);
         default:
            return this.unknownLength;
      }
   }

   @Override
   public int getMaximumPrecision(int oid) {
      oid = this.convertArrayToBaseOid(oid);
      switch (oid) {
         case 1042:
         case 1043:
            return 10485760;
         case 1083:
         case 1266:
            return 6;
         case 1114:
         case 1184:
         case 1186:
            return 6;
         case 1560:
         case 1562:
            return 83886080;
         case 1700:
            return 1000;
         default:
            return 0;
      }
   }

   @Override
   public boolean requiresQuoting(int oid) throws SQLException {
      int sqlType = this.getSQLType(oid);
      return this.requiresQuotingSqlType(sqlType);
   }

   @Override
   public boolean requiresQuotingSqlType(int sqlType) throws SQLException {
      switch (sqlType) {
         case -6:
         case -5:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
            return false;
         case -4:
         case -3:
         case -2:
         case -1:
         case 0:
         case 1:
         default:
            return true;
      }
   }

   @Override
   public int longOidToInt(long oid) throws SQLException {
      if ((oid & -4294967296L) != 0L) {
         throw new PSQLException(GT.tr("Value is not an OID: {0}", oid), PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
      } else {
         return (int)oid;
      }
   }

   @Override
   public long intOidToLong(int oid) {
      return oid & 4294967295L;
   }

   static {
      TYPE_ALIASES.put("bool", "bool");
      TYPE_ALIASES.put("boolean", "bool");
      TYPE_ALIASES.put("smallint", "int2");
      TYPE_ALIASES.put("int2", "int2");
      TYPE_ALIASES.put("int", "int4");
      TYPE_ALIASES.put("integer", "int4");
      TYPE_ALIASES.put("int4", "int4");
      TYPE_ALIASES.put("long", "int8");
      TYPE_ALIASES.put("int8", "int8");
      TYPE_ALIASES.put("bigint", "int8");
      TYPE_ALIASES.put("float", "float8");
      TYPE_ALIASES.put("real", "float4");
      TYPE_ALIASES.put("float4", "float4");
      TYPE_ALIASES.put("double", "float8");
      TYPE_ALIASES.put("double precision", "float8");
      TYPE_ALIASES.put("float8", "float8");
      TYPE_ALIASES.put("decimal", "numeric");
      TYPE_ALIASES.put("numeric", "numeric");
      TYPE_ALIASES.put("character varying", "varchar");
      TYPE_ALIASES.put("varchar", "varchar");
      TYPE_ALIASES.put("time without time zone", "time");
      TYPE_ALIASES.put("time", "time");
      TYPE_ALIASES.put("time with time zone", "timetz");
      TYPE_ALIASES.put("timetz", "timetz");
      TYPE_ALIASES.put("timestamp without time zone", "timestamp");
      TYPE_ALIASES.put("timestamp", "timestamp");
      TYPE_ALIASES.put("timestamp with time zone", "timestamptz");
      TYPE_ALIASES.put("timestamptz", "timestamptz");
   }
}
