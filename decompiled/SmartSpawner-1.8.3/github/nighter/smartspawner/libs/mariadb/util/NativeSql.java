package github.nighter.smartspawner.libs.mariadb.util;

import github.nighter.smartspawner.libs.mariadb.client.Context;
import java.sql.SQLException;
import java.util.Locale;

public final class NativeSql {
   public static String parse(String sql, Context context) throws SQLException {
      if (!sql.contains("{")) {
         return sql;
      } else {
         ClientParser.LexState state = ClientParser.LexState.Normal;
         char lastChar = 0;
         boolean singleQuotes = false;
         int lastEscapePart = 0;
         StringBuilder sb = new StringBuilder();
         char[] query = sql.toCharArray();
         int queryLength = query.length;
         int escapeIdx = 0;
         boolean inEscape = false;

         for (int idx = 0; idx < queryLength; idx++) {
            char car = query[idx];
            if (state != ClientParser.LexState.Escape || car == '\'' && singleQuotes || car == '"' && !singleQuotes) {
               switch (car) {
                  case '\n':
                     if (state == ClientParser.LexState.EOLComment) {
                        state = ClientParser.LexState.Normal;
                     }
                     break;
                  case '"':
                     if (state == ClientParser.LexState.Normal) {
                        state = ClientParser.LexState.String;
                        singleQuotes = false;
                     } else if (state == ClientParser.LexState.String && !singleQuotes) {
                        state = ClientParser.LexState.Normal;
                     } else if (state == ClientParser.LexState.Escape) {
                        state = ClientParser.LexState.String;
                     }
                     break;
                  case '#':
                     if (state == ClientParser.LexState.Normal) {
                        state = ClientParser.LexState.EOLComment;
                     }
                     break;
                  case '\'':
                     if (state == ClientParser.LexState.Normal) {
                        state = ClientParser.LexState.String;
                        singleQuotes = true;
                     } else if (state == ClientParser.LexState.String && singleQuotes) {
                        state = ClientParser.LexState.Normal;
                     } else if (state == ClientParser.LexState.Escape) {
                        state = ClientParser.LexState.String;
                     }
                     break;
                  case '*':
                     if (state == ClientParser.LexState.Normal && lastChar == '/') {
                        state = ClientParser.LexState.SlashStarComment;
                     }
                     break;
                  case '-':
                     if (state == ClientParser.LexState.Normal && lastChar == '-') {
                        state = ClientParser.LexState.EOLComment;
                     }
                     break;
                  case '/':
                     if (state == ClientParser.LexState.SlashStarComment && lastChar == '*') {
                        state = ClientParser.LexState.Normal;
                     } else if (state == ClientParser.LexState.Normal && lastChar == '/') {
                        state = ClientParser.LexState.EOLComment;
                     }
                     break;
                  case '\\':
                     if (state == ClientParser.LexState.String) {
                        state = ClientParser.LexState.Escape;
                     }
                     break;
                  case '`':
                     if (state == ClientParser.LexState.Backtick) {
                        state = ClientParser.LexState.Normal;
                     } else if (state == ClientParser.LexState.Normal) {
                        state = ClientParser.LexState.Backtick;
                     }
                     break;
                  case '{':
                     if (state == ClientParser.LexState.Normal) {
                        if (!inEscape) {
                           inEscape = true;
                           lastEscapePart = idx;
                        }

                        escapeIdx++;
                     }
                     break;
                  case '}':
                     if (state == ClientParser.LexState.Normal && inEscape) {
                        if (--escapeIdx == 0) {
                           String str = sql.substring(lastEscapePart, idx + 1);
                           String escapedSeq = resolveEscapes(str, context);
                           sb.append(escapedSeq);
                           inEscape = false;
                           continue;
                        }
                     }
               }

               if (!inEscape) {
                  sb.append(car);
               }

               lastChar = car;
            } else {
               state = ClientParser.LexState.String;
               if (!inEscape) {
                  sb.append(car);
               }

               lastChar = car;
            }
         }

         if (inEscape) {
            throw new SQLException("Invalid escape sequence , missing closing '}' character in '" + sql + "'");
         } else {
            return sb.toString();
         }
      }
   }

   private static String resolveEscapes(String escaped, Context context) throws SQLException {
      int endIndex = escaped.length() - 1;
      if (escaped.startsWith("{fn ")) {
         String resolvedParams = replaceFunctionParameter(escaped.substring(4, endIndex), context);
         return parse(resolvedParams, context);
      } else if (escaped.startsWith("{oj ")) {
         return parse(escaped.substring(4, endIndex), context);
      } else if (escaped.startsWith("{d ")) {
         return escaped.substring(3, endIndex);
      } else if (escaped.startsWith("{t ")) {
         return escaped.substring(3, endIndex);
      } else if (escaped.startsWith("{ts ")) {
         return escaped.substring(4, endIndex);
      } else if (escaped.startsWith("{d'")) {
         return escaped.substring(2, endIndex);
      } else if (escaped.startsWith("{t'")) {
         return escaped.substring(2, endIndex);
      } else if (escaped.startsWith("{ts'")) {
         return escaped.substring(3, endIndex);
      } else if (escaped.startsWith("{call ") || escaped.startsWith("{CALL ")) {
         return parse(escaped.substring(1, endIndex), context);
      } else if (escaped.startsWith("{?")) {
         return parse(escaped.substring(1, endIndex), context);
      } else {
         if (!escaped.startsWith("{ ") && !escaped.startsWith("{\n")) {
            if (escaped.startsWith("{\r\n")) {
               for (int i = 3; i < escaped.length(); i++) {
                  if (!Character.isWhitespace(escaped.charAt(i))) {
                     return resolveEscapes("{" + escaped.substring(i), context);
                  }
               }
            }
         } else {
            for (int ix = 2; ix < escaped.length(); ix++) {
               if (!Character.isWhitespace(escaped.charAt(ix))) {
                  return resolveEscapes("{" + escaped.substring(ix), context);
               }
            }
         }

         throw new SQLException("unknown escape sequence " + escaped);
      }
   }

   private static String replaceFunctionParameter(String functionString, Context context) {
      char[] input = functionString.toCharArray();
      StringBuilder sb = new StringBuilder();
      int index = 0;

      while (index < input.length && input[index] == ' ') {
         index++;
      }

      while (index < input.length && (input[index] >= 'a' && input[index] <= 'z' || input[index] >= 'A' && input[index] <= 'Z')) {
         sb.append(input[index]);
         index++;
      }

      String func = sb.toString().toLowerCase(Locale.ROOT);
      switch (func) {
         case "convert":
            int lastCommaIndex = functionString.lastIndexOf(44);
            int firstParentheses = functionString.indexOf(40);
            String value = functionString.substring(firstParentheses + 1, lastCommaIndex);
            index = lastCommaIndex + 1;

            while (index < input.length && Character.isWhitespace(input[index])) {
               index++;
            }

            int endParam = index + 1;

            while (
               endParam < input.length
                  && (input[endParam] >= 'a' && input[endParam] <= 'z' || input[endParam] >= 'A' && input[endParam] <= 'Z' || input[endParam] == '_')
            ) {
               endParam++;
            }

            String typeParam = new String(input, index, endParam - index).toUpperCase(Locale.ROOT);
            if (typeParam.startsWith("SQL_")) {
               typeParam = typeParam.substring(4);
            }

            switch (typeParam) {
               case "BOOLEAN":
                  return "1=" + value;
               case "BIGINT":
               case "SMALLINT":
               case "TINYINT":
                  typeParam = "SIGNED INTEGER";
                  break;
               case "BIT":
                  typeParam = "UNSIGNED INTEGER";
                  break;
               case "BLOB":
               case "VARBINARY":
               case "LONGVARBINARY":
               case "ROWID":
                  typeParam = "BINARY";
                  break;
               case "NCHAR":
               case "CLOB":
               case "NCLOB":
               case "DATALINK":
               case "VARCHAR":
               case "NVARCHAR":
               case "LONGVARCHAR":
               case "LONGNVARCHAR":
               case "SQLXML":
               case "LONGNCHAR":
                  typeParam = "CHAR";
                  break;
               case "DOUBLE":
               case "FLOAT":
                  if (!context.getVersion().isMariaDBServer() && !context.getVersion().versionGreaterOrEqual(8, 0, 17)) {
                     return "0.0+" + value;
                  }

                  typeParam = "DOUBLE";
                  break;
               case "REAL":
               case "NUMERIC":
                  typeParam = "DECIMAL";
                  break;
               case "TIMESTAMP":
                  typeParam = "DATETIME";
            }

            return new String(input, 0, index) + typeParam + new String(input, endParam, input.length - endParam);
         case "timestampdiff":
         case "timestampadd":
            while (index < input.length && (Character.isWhitespace(input[index]) || input[index] == '(')) {
               index++;
            }

            if (index < input.length - 8) {
               String paramPrefix = new String(input, index, 8);
               if ("SQL_TSI_".equals(paramPrefix)) {
                  return new String(input, 0, index) + new String(input, index + 8, input.length - (index + 8));
               }
            }

            return functionString;
         default:
            return functionString;
      }
   }
}
