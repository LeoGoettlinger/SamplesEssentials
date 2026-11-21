package com.mysql.cj.log;

import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.util.Util;

public class LogFactory {
   public static Log getLogger(String className, String instanceName) {
      if (className == null) {
         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, "Logger class can not be NULL");
      } else if (instanceName == null) {
         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, "Logger instance name can not be NULL");
      } else {
         try {
            return Util.getInstance(Log.class, className, new Class[]{String.class}, new Object[]{instanceName}, null);
         } catch (CJException var5) {
            if (ClassNotFoundException.class.isInstance(var5.getCause())) {
               try {
                  return Util.getInstance(
                     Log.class, Util.getPackageName(LogFactory.class) + "." + className, new Class[]{String.class}, new Object[]{instanceName}, null
                  );
               } catch (CJException var4) {
                  throw var5;
               }
            } else {
               throw var5;
            }
         }
      }
   }
}
