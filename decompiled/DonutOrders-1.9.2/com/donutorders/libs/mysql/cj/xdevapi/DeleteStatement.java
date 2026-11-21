package com.donutorders.libs.mysql.cj.xdevapi;

public interface DeleteStatement extends Statement<DeleteStatement, Result> {
   DeleteStatement where(String var1);

   DeleteStatement orderBy(String... var1);

   DeleteStatement limit(long var1);
}
