/*    */ package common;
/*    */ 
/*    */ import com.rushteamc.lib.SharedEventBus.SharedEventBus;
/*    */ import com.rushteamc.plugin.common.Assembler;
/*    */ import com.rushteamc.plugin.common.Events.PlayerJoinWorldEvent;
/*    */ import com.rushteamc.plugin.common.FormattedString.FormattedString;
/*    */ import com.rushteamc.plugin.common.FormattedString.FormattedString.ParseErrorException;
/*    */ import com.rushteamc.plugin.common.Group;
/*    */ import com.rushteamc.plugin.common.Player;
/*    */ import com.rushteamc.plugin.common.World;
/*    */ import java.io.PrintStream;
/*    */ import java.util.HashSet;
/*    */ import java.util.Set;
/*    */ import org.junit.Test;
/*    */ 
/*    */ public class DatabaseTest
/*    */ {
/*    */   @Test
/*    */   public void databaseTest()
/*    */   {
/*    */     try
/*    */     {
/* 22 */       FormattedString str = new FormattedString(null, "some test str");
/* 23 */       System.out.println(str);
/* 24 */       str.replace("test", new FormattedString(null, "..."));
/* 25 */       System.out.println(str);
/*    */     } catch (FormattedString.ParseErrorException e1) {
/* 27 */       e1.printStackTrace();
/*    */     }
/*    */ 
/* 30 */     Set hosts = new HashSet();
/* 31 */     hosts.add("localhost:8081");
/* 32 */     Assembler setup = new Assembler(hosts, "localhost", "RTMCCommon", "root", "2112", "group", "pass", null, null, null);
/*    */ 
/* 35 */     Player playerSTS = new Player("STS");
/* 36 */     setup.getSharedEventBus().postEvent(new PlayerJoinWorldEvent(playerSTS, World.getWorld("testworld")));
/*    */ 
/* 41 */     Player player = new Player("STS");
/* 42 */     System.out.println("Player ID: " + player.getID());
/*    */ 
/* 44 */     player.addPermission("plugin.test", setup);
/* 45 */     player.addPermission("plugin.tester", setup);
/* 46 */     player.unsetPermission("plugin.test", setup);
/*    */ 
/* 48 */     Player player2 = new Player("STSc");
/* 49 */     System.out.println("Player ID: " + player2.getID());
/*    */ 
/* 51 */     System.out.println("STS " + player.getPermissions());
/* 52 */     System.out.println("STSc " + player2.getPermissions());
/*    */ 
/* 56 */     System.out.println("Get group Admins...");
/* 57 */     Group group1 = new Group("Admins");
/* 58 */     System.out.println("Get group Users...");
/* 59 */     Group group2 = new Group("Users");
/*    */ 
/* 61 */     group2.addPermission("plugin.use", setup);
/* 62 */     group1.addPermission("plugin.administrate", setup);
/*    */ 
/* 64 */     group1.addParent(group2, setup);
/*    */ 
/* 66 */     System.out.println("Admins group " + group1.getPermissions());
/*    */ 
/* 68 */     System.out.println("STS " + player.getPermissions());
/* 69 */     System.out.println("STSc " + player2.getPermissions());
/*    */     try
/*    */     {
/* 72 */       Thread.sleep(500L);
/*    */     } catch (InterruptedException e) {
/* 74 */       e.printStackTrace();
/*    */     }
/*    */   }
/*    */ }

/* Location:           /home/steven/RTMC/backup/target/test-classes/
 * Qualified Name:     common.DatabaseTest
 * JD-Core Version:    0.6.2
 */