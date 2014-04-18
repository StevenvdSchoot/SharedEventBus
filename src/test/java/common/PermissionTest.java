/*     */ package common;
/*     */ 
/*     */ import com.rushteamc.plugin.common.Permissions.Permission;
/*     */ import com.rushteamc.plugin.common.Permissions.PermissionSet;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.ObjectInputStream;
/*     */ import java.io.ObjectOutputStream;
/*     */ import java.io.PrintStream;
/*     */ import org.junit.Test;
/*     */ 
/*     */ public class PermissionTest
/*     */ {
/*     */   @Test
/*     */   public void permissionTest()
/*     */   {
/*  22 */     PermissionSet permissionSet = new PermissionSet();
/*     */ 
/*  24 */     permissionSet.setPermission(new Permission("AntiBot.admin.root", Boolean.valueOf(true)));
/*  25 */     permissionSet.setPermission(new Permission("bukkit.command.gamemode", Boolean.valueOf(true)));
/*  26 */     permissionSet.setPermission(new Permission("bukkit.command.save", Boolean.valueOf(true)));
/*  27 */     permissionSet.setPermission(new Permission("disabler", Boolean.valueOf(true)));
/*  28 */     permissionSet.setPermission(new Permission("dynmap", Boolean.valueOf(true)));
/*  29 */     permissionSet.setPermission(new Permission("eliteblocker", Boolean.valueOf(true)));
/*  30 */     permissionSet.setPermission(new Permission("entitymanager", Boolean.valueOf(true)));
/*  31 */     permissionSet.setPermission(new Permission("essentials", Boolean.valueOf(true)));
/*  32 */     permissionSet.setPermission(new Permission("logblock", Boolean.valueOf(true)));
/*  33 */     permissionSet.setPermission(new Permission("mineconomy", Boolean.valueOf(true)));
/*  34 */     permissionSet.setPermission(new Permission("modifyworld", Boolean.valueOf(true)));
/*  35 */     permissionSet.setPermission(new Permission("multiverse", Boolean.valueOf(true)));
/*  36 */     permissionSet.setPermission(new Permission("OpenInv.exempt", Boolean.valueOf(true)));
/*  37 */     permissionSet.setPermission(new Permission("OpenInv.override", Boolean.valueOf(true)));
/*  38 */     permissionSet.setPermission(new Permission("rtmc", Boolean.valueOf(true)));
/*  39 */     permissionSet.setPermission(new Permission("worldborder", Boolean.valueOf(true)));
/*  40 */     permissionSet.setPermission(new Permission("worldedit", Boolean.valueOf(true)));
/*  41 */     permissionSet.setPermission(new Permission("worldguard", Boolean.valueOf(true)));
/*     */ 
/*  43 */     System.out.println(permissionSet);
/*     */ 
/*  47 */     Permission perm = new Permission("com.rushteamc.test");
/*  48 */     permissionSet.addPermission(perm);
/*     */ 
/*  50 */     perm = new Permission("com.rushteamc.testmore");
/*  51 */     permissionSet.addPermission(perm);
/*     */ 
/*  53 */     perm = new Permission("rtmc.adminchat");
/*  54 */     permissionSet.addPermission(perm);
/*     */ 
/*  56 */     perm = new Permission("rtmc");
/*  57 */     permissionSet.addPermission(perm);
/*     */ 
/*  59 */     perm = new Permission("net.testing");
/*  60 */     permissionSet.addPermission(perm);
/*     */ 
/*  62 */     perm = new Permission("com.rushteamc");
/*  63 */     Permission[] permissionChanges = permissionSet.removePermission(perm);
/*  64 */     for (Permission permissionChange : permissionChanges) {
/*  65 */       System.out.println("Changed: " + permissionChange);
/*     */     }
/*  67 */     System.out.println(permissionSet);
/*     */ 
/*  70 */     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
ObjectOutputStream objectOutputStream;
/*     */     try
/*     */     {
/*  73 */       objectOutputStream = new ObjectOutputStream(byteArrayOutputStream); } catch (IOException e) {
/*  75 */       e.printStackTrace();
/*     */       return;
/*     */     }
/*     */     try { objectOutputStream.writeObject(permissionSet);
/*     */     } catch (IOException e1) {
/*  82 */       e1.printStackTrace();
/*     */     }
/*     */     try
/*     */     {
/*  86 */       objectOutputStream.flush();
/*     */     } catch (IOException e1) {
/*  88 */       e1.printStackTrace();
/*     */     }
/*     */     try {
/*  91 */       byteArrayOutputStream.flush();
/*     */     } catch (IOException e1) {
/*  93 */       e1.printStackTrace();
/*     */     }
/*     */ 
/*  96 */     byte[] result = byteArrayOutputStream.toByteArray();
/*     */     try
/*     */     {
/*  99 */       objectOutputStream.close();
/*     */     } catch (IOException e) {
/* 101 */       e.printStackTrace();
/*     */     }
/*     */     try {
/* 104 */       byteArrayOutputStream.close();
/*     */     } catch (IOException e) {
/* 106 */       e.printStackTrace();
/*     */     }
/*     */ 
/* 109 */     ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(result);
ObjectInputStream objectInputStream;
/*     */     try
/*     */     {
/* 112 */       objectInputStream = new ObjectInputStream(byteArrayInputStream); } catch (IOException e) {
/* 114 */       e.printStackTrace();
/*     */       return;
/*     */     }
/*     */     try { Object obj = objectInputStream.readObject();
/*     */ 
/* 121 */       if (!(obj instanceof PermissionSet)) {
/* 122 */         return;
/*     */       }
/* 124 */       PermissionSet permSet = (PermissionSet)obj;
/*     */ 
/* 126 */       System.out.println(permSet);
/*     */     } catch (ClassNotFoundException e1) {
/* 128 */       e1.printStackTrace();
/*     */     } catch (IOException e1) {
/* 130 */       e1.printStackTrace();
/*     */     }
/*     */     try
/*     */     {
/* 134 */       objectInputStream.close();
/*     */     } catch (IOException e) {
/* 136 */       e.printStackTrace();
/*     */     }
/*     */     try {
/* 139 */       byteArrayInputStream.close();
/*     */     } catch (IOException e) {
/* 141 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/steven/RTMC/backup/target/test-classes/
 * Qualified Name:     common.PermissionTest
 * JD-Core Version:    0.6.2
 */