package com.mysql.cj.jdbc.ha;

import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.mysql.cj.util.StringUtils;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class StandardLoadBalanceExceptionChecker implements LoadBalanceExceptionChecker {
   private List<String> sqlStateList;
   private List<Class<?>> sqlExClassList;

   @Override
   public boolean shouldExceptionTriggerFailover(Throwable ex) {
      String sqlState = ex instanceof SQLException ? ((SQLException)ex).getSQLState() : null;
      if (sqlState != null) {
         if (sqlState.startsWith("08")) {
            return true;
         }

         if (this.sqlStateList != null) {
            Iterator<String> i = this.sqlStateList.iterator();

            while (i.hasNext()) {
               if (sqlState.startsWith(i.next().toString())) {
                  return true;
               }
            }
         }
      }

      if (!(ex instanceof CommunicationsException) && !(ex instanceof CJCommunicationsException)) {
         if (this.sqlExClassList != null) {
            Iterator<Class<?>> i = this.sqlExClassList.iterator();

            while (i.hasNext()) {
               if (i.next().isInstance(ex)) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   @Override
   public void destroy() {
   }

   @Override
   public void init(Properties props) {
      this.configureSQLStateList(props.getProperty(PropertyKey.loadBalanceSQLStateFailover.getKeyName(), null));
      this.configureSQLExceptionSubclassList(props.getProperty(PropertyKey.loadBalanceSQLExceptionSubclassFailover.getKeyName(), null));
   }

   private void configureSQLStateList(String sqlStates) {
      if (sqlStates != null && !"".equals(sqlStates)) {
         this.sqlStateList = StringUtils.split(sqlStates, ",", true).stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
      }
   }

   private void configureSQLExceptionSubclassList(String sqlExClasses) {
      if (sqlExClasses != null && !"".equals(sqlExClasses)) {
         this.sqlExClassList = StringUtils.split(sqlExClasses, ",", true).stream().filter(s -> !s.isEmpty()).map(s -> {
            try {
               return Class.forName(s, false, this.getClass().getClassLoader());
            } catch (ClassNotFoundException var3) {
               return null;
            }
         }).filter(Objects::nonNull).collect(Collectors.toList());
      }
   }
}
