/*     */ package common;
/*     */ 
/*     */ import com.rushteamc.lib.SharedEventBus.SharedEventBus;
/*     */ import com.rushteamc.lib.SharedEventBus.Subscribe;
/*     */ import com.rushteamc.plugin.common.Authentication.Authenticator;
/*     */ import com.rushteamc.plugin.common.FormattedString.FormattedString;
/*     */ import com.rushteamc.plugin.common.FormattedString.FormattedString.FormattedStringPiece;
/*     */ import com.rushteamc.plugin.common.FormattedString.FormattedString.ParseErrorException;
/*     */ import com.rushteamc.plugin.common.FormattedString.FormattedString.Style;
/*     */ import com.rushteamc.plugin.common.FormattedString.StringFormatter;
/*     */ import java.awt.Color;
/*     */ import java.io.PrintStream;
/*     */ import java.util.List;
/*     */ import org.junit.Test;
/*     */ 
/*     */ public class FormattedStringTest
/*     */ {
/*  19 */   int continueCounter = 0;
/*     */ 
/*     */   @Test
/*     */   public void formattedStringTest()
/*     */   {
/*  24 */     byte[] testHash = Authenticator.stringToHash("c6001d5b2ac3df314204a8f9d7a00e1503c9aba0fd4538645de4bf4cc7e2555cfe9ff9d0236bf327ed3e907849a98df4d330c4bea551017d465b4c1d9b80bcb0");
/*  25 */     System.out.println("c6001d5b2ac3df314204a8f9d7a00e1503c9aba0fd4538645de4bf4cc7e2555cfe9ff9d0236bf327ed3e907849a98df4d330c4bea551017d465b4c1d9b80bcb0");
/*  26 */     System.out.println(Authenticator.hashToString(testHash));
/*     */ 
/*  28 */     FormattedString.addFormatter("format1", new StringFormatter1());
/*  29 */     FormattedString.addFormatter("format2", new StringFormatter2());
/*     */     try
/*     */     {
/*  32 */       FormattedString test = new FormattedString("save", "203040f200000007Knight ");
/*  33 */       System.out.println("Result: " + test.toString("save"));
/*     */     } catch (FormattedString.ParseErrorException e) {
/*  35 */       e.printStackTrace();
/*     */     }
/*     */     try
/*     */     {
/*  39 */       FormattedString format = new FormattedString("format1", "&f[&9$3&f][&9$4&f][&9$1&f]: $2");
/*  40 */       FormattedString text1 = new FormattedString("format1", "text1");
/*  41 */       FormattedString text2 = new FormattedString("format1", "text2");
/*  42 */       FormattedString text3 = new FormattedString("format1", "text3");
/*  43 */       FormattedString text4 = new FormattedString("format1", "text4");
/*  44 */       System.out.println(format.toString());
/*  45 */       FormattedString result = FormattedString.Format(format, new FormattedString[] { text1, text2, text3, text4 });
/*  46 */       System.out.println(result.toString("format1"));
/*     */     } catch (FormattedString.ParseErrorException e2) {
/*  48 */       e2.printStackTrace();
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  54 */       FormattedString format = new FormattedString("format1", "&4[$1&4]: $6");
/*     */ 
/*  56 */       FormattedString text1 = new FormattedString("format1", "text1");
/*  57 */       FormattedString text2 = new FormattedString();
/*  58 */       FormattedString text3 = new FormattedString();
/*  59 */       FormattedString text4 = new FormattedString("format1", "text4");
/*  60 */       FormattedString text5 = new FormattedString("format1", "text3");
/*  61 */       FormattedString text6 = new FormattedString("format1", "text4");
/*     */ 
/*  63 */       FormattedString result = FormattedString.Format(format, new FormattedString[] { text1, text2, text3, text4, text5, text6 });
/*  64 */       System.out.println(result.toString("format1"));
/*     */     } catch (FormattedString.ParseErrorException e) {
/*  66 */       e.printStackTrace();
/*     */     }
/*     */ 
/*     */     FormattedString formattedString1;
/*     */     try
/*     */     {
/*  74 */       formattedString1 = new FormattedString("format1", "&1This &8is a test string!");
/*     */     }
/*     */     catch (FormattedString.ParseErrorException e1)
/*     */     {
/*  76 */       e1.printStackTrace();
/*     */       return;
/*     */     }
/*  80 */     SharedEventBus eventbus = new SharedEventBus();
/*  81 */     eventbus.addHandler(new SharedEventBusHandler());
/*  82 */     eventbus.postEvent(formattedString1);
/*     */ 
/*  84 */     while (this.continueCounter < 1)
/*     */       try {
/*  86 */         Thread.sleep(10L);
/*     */       } catch (InterruptedException e) {
/*  88 */         e.printStackTrace();
/*     */       }
/*     */   }
/*     */ 
/*     */   public class SharedEventBusHandler {
/*     */     public SharedEventBusHandler() {
/*     */     }
/*     */ 
/*     */     @Subscribe(instanceOf=true)
/*     */     public void onFormattedString(FormattedString formattedString) {
/*  98 */       System.out.println(formattedString.toString());
/*  99 */       System.out.println(formattedString.toString("format2"));
/* 100 */       System.out.println(formattedString.toString("format1"));
/*     */ 
/* 102 */       FormattedStringTest.this.continueCounter += 1;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static class StringFormatter1 implements StringFormatter
/*     */   {
/* 108 */     private static final FormattedString.Style defaultStyle = new FormattedString.Style(Color.white, Boolean.valueOf(false), Boolean.valueOf(false), Boolean.valueOf(false), Boolean.valueOf(false), Boolean.valueOf(false));
/*     */ 
/*     */     public void append(List<FormattedString.FormattedStringPiece> formattedStringPieces, String text) throws FormattedString.ParseErrorException
/*     */     {
/* 112 */       int i = 0;
/* 113 */       int startPiece = 0;
/* 114 */       boolean foundFormatter = false;
/* 115 */       FormattedString.Style style = new FormattedString.Style();
/* 116 */       while (i < text.length())
/*     */       {
/* 118 */         if (text.charAt(i) == '&')
/*     */         {
/* 120 */           foundFormatter = true;
/* 121 */           i++;
/*     */         }
/* 125 */         else if (foundFormatter)
/*     */         {
/* 127 */           foundFormatter = false;
/*     */ 
/* 129 */           if (startPiece < i - 1)
/*     */           {
/* 131 */             FormattedString.FormattedStringPiece formattedStringPiece = new FormattedString.FormattedStringPiece(style, text.substring(startPiece, i - 1));
/* 132 */             formattedStringPieces.add(formattedStringPiece);
/* 133 */             style = new FormattedString.Style();
/*     */           }
/*     */ 
/* 136 */           char chr = text.charAt(i);
/* 137 */           if (('0' <= chr) && (chr <= 'f'))
/*     */           {
/* 139 */             int scale = Integer.parseInt(String.valueOf(chr), 16);
/* 140 */             scale = scale << 4 | scale;
/* 141 */             scale = scale << 16 | scale << 8 | scale;
/* 142 */             style.setColor(new Color(scale));
/*     */           }
/* 144 */           else if (chr == 'g')
/*     */           {
/* 146 */             style.setBold(Boolean.valueOf(true));
/*     */           }
/* 148 */           else if (chr == 'h')
/*     */           {
/* 150 */             style.setItalic(Boolean.valueOf(true));
/*     */           }
/* 152 */           else if (chr == 'r')
/*     */           {
/* 154 */             style.setBold(Boolean.valueOf(false));
/* 155 */             style.setItalic(Boolean.valueOf(false));
/*     */           }
/*     */ 
/* 158 */           i++;
/* 159 */           startPiece = i;
/*     */         }
/*     */         else
/*     */         {
/* 163 */           i++;
/*     */         }
/*     */       }
/* 165 */       if (startPiece < i - 1)
/*     */       {
/* 167 */         FormattedString.FormattedStringPiece formattedStringPiece = new FormattedString.FormattedStringPiece(style, text.substring(startPiece, i));
/* 168 */         formattedStringPieces.add(formattedStringPiece);
/*     */       }
/*     */     }
/*     */ 
/*     */     public String getFormattedString(List<FormattedString.FormattedStringPiece> formattedStringPieces)
/*     */     {
/* 174 */       FormattedString.Style currentStyle = defaultStyle;
/* 175 */       StringBuilder stringBuilder = new StringBuilder();
/* 176 */       for (FormattedString.FormattedStringPiece formattedStringPiece : formattedStringPieces)
/*     */       {
/* 178 */         FormattedString.Style style = formattedStringPiece.getStyle();
/*     */ 
/* 180 */         if (style.getColor() != null)
/*     */         {
/* 182 */           stringBuilder.append('&');
/* 183 */           stringBuilder.append(Integer.toHexString((style.getColor().getBlue() + style.getColor().getRed() + style.getColor().getBlue()) / 48).charAt(0));
/*     */         }
/*     */ 
/* 186 */         if (style.getBold() != null) {
/* 187 */           if ((style.getBold().booleanValue()) && (!currentStyle.getBold().booleanValue()))
/*     */           {
/* 189 */             stringBuilder.append('&');
/* 190 */             stringBuilder.append('g');
/* 191 */             currentStyle.setBold(Boolean.valueOf(true));
/*     */           }
/* 193 */           else if ((!style.getBold().booleanValue()) && (currentStyle.getBold().booleanValue()))
/*     */           {
/* 195 */             stringBuilder.append('&');
/* 196 */             stringBuilder.append('r');
/* 197 */             currentStyle.setBold(Boolean.valueOf(false));
/* 198 */             printCurrent(stringBuilder, currentStyle);
/*     */           }
/*     */         }
/* 201 */         if (style.getItalic() != null) {
/* 202 */           if ((style.getItalic().booleanValue()) && (!currentStyle.getItalic().booleanValue()))
/*     */           {
/* 204 */             stringBuilder.append('&');
/* 205 */             stringBuilder.append('h');
/* 206 */             currentStyle.setItalic(Boolean.valueOf(true));
/*     */           }
/* 208 */           else if ((!style.getItalic().booleanValue()) && (currentStyle.getItalic().booleanValue()))
/*     */           {
/* 210 */             stringBuilder.append('&');
/* 211 */             stringBuilder.append('r');
/* 212 */             currentStyle.setItalic(Boolean.valueOf(false));
/* 213 */             printCurrent(stringBuilder, currentStyle);
/*     */           }
/*     */         }
/* 216 */         stringBuilder.append(formattedStringPiece.getText());
/*     */       }
/*     */ 
/* 219 */       return stringBuilder.toString();
/*     */     }
/*     */ 
/*     */     private void printCurrent(StringBuilder stringBuilder, FormattedString.Style style)
/*     */     {
/* 224 */       if (style.getBold().booleanValue())
/*     */       {
/* 226 */         stringBuilder.append('&');
/* 227 */         stringBuilder.append('g');
/*     */       }
/*     */ 
/* 230 */       if (style.getItalic().booleanValue())
/*     */       {
/* 232 */         stringBuilder.append('&');
/* 233 */         stringBuilder.append('h');
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static class StringFormatter2 implements StringFormatter
/*     */   {
/*     */     public void append(List<FormattedString.FormattedStringPiece> formattedStringPieces, String text) throws FormattedString.ParseErrorException
/*     */     {
/* 242 */       formattedStringPieces.add(new FormattedString.FormattedStringPiece(new FormattedString.Style(), text));
/*     */     }
/*     */ 
/*     */     public String getFormattedString(List<FormattedString.FormattedStringPiece> formattedStringPieces)
/*     */     {
/* 247 */       String str = "";
/* 248 */       for (FormattedString.FormattedStringPiece formattedStringPiece : formattedStringPieces)
/* 249 */         str = str + formattedStringPiece.getText();
/* 250 */       return str;
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/steven/RTMC/backup/target/test-classes/
 * Qualified Name:     common.FormattedStringTest
 * JD-Core Version:    0.6.2
 */