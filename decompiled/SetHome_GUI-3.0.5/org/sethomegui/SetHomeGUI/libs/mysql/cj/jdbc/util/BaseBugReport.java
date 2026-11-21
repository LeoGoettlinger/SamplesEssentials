package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.Driver;

public abstract class BaseBugReport {
   private Connection conn;
   private Driver driver;
   private final Lock lock = new ReentrantLock();

   public BaseBugReport() {
      try {
         this.driver = new Driver();
      } catch (SQLException var2) {
         throw new RuntimeException(var2.toString());
      }
   }

   public abstract void setUp() throws Exception;

   public abstract void tearDown() throws Exception;

   public abstract void runTest() throws Exception;

   public final void run() throws Exception {
      try {
         this.setUp();
         this.runTest();
      } finally {
         this.tearDown();
      }
   }

   protected final void assertTrue(String message, boolean condition) throws Exception {
      if (!condition) {
         throw new Exception("Assertion failed: " + message);
      }
   }

   protected final void assertTrue(boolean condition) throws Exception {
      this.assertTrue("(no message given)", condition);
   }

   public String getUrl() {
      return "jdbc:mysql:///test";
   }

   public final Connection getConnection() throws SQLException {
      this.lock.lock();

      Connection var1;
      try {
         if (this.conn == null || this.conn.isClosed()) {
            this.conn = this.getNewConnection();
         }

         var1 = this.conn;
      } finally {
         this.lock.unlock();
      }

      return var1;
   }

   public final Connection getNewConnection() throws SQLException {
      this.lock.lock();

      Connection var1;
      try {
         var1 = this.getConnection(this.getUrl());
      } finally {
         this.lock.unlock();
      }

      return var1;
   }

   public final Connection getConnection(String url) throws SQLException {
      this.lock.lock();

      Connection var2;
      try {
         var2 = this.getConnection(url, null);
      } finally {
         this.lock.unlock();
      }

      return var2;
   }

   public final Connection getConnection(String url, Properties props) throws SQLException {
      this.lock.lock();

      Connection var3;
      try {
         var3 = this.driver.connect(url, props);
      } finally {
         this.lock.unlock();
      }

      return var3;
   }
}
