package com.integration.util;
import java.util.Map;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
public class ExcelUtil {
    
    public static void export(String path,String title,List<Map<String, String>> fields,List<?> results,boolean isNumOpen){
        try {
            int celladd=0;
            if(isNumOpen) {
                celladd=1;
            }
            HSSFWorkbook wb = new HSSFWorkbook();
            // 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet sheet = wb.createSheet(title);
            // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            for(int i=celladd;i<fields.size();i++) {
                sheet.setColumnWidth(i, 15 * 256);
            }
            // 第四步，创建单元格，并设置值表头 设置表头居中
            HSSFRow row = sheet.createRow(0);
            HSSFCellStyle style = wb.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER); // 创建一个居中格式
            HSSFCell cell=null;
            if(isNumOpen) {
                cell = row.createCell((short)0);
                cell.setCellValue("序号");
                cell.setCellStyle(style); 
            }
            for(int i=0;i<fields.size();i++) {
                cell = row.createCell((short)i+celladd); 
                cell.setCellValue(fields.get(i).get("title"));
                cell.setCellStyle(style);
            }
    
            for (int i=0;i<results.size();i++) {
                    row = sheet.createRow((int) i + 1);
                    if(isNumOpen) {
                        row.createCell((short) 0).setCellValue((i + 1));
                    }
                    Object obj = results.get(i);
                    for(int j=0;j<fields.size();j++) {
                        Field f = obj.getClass().getDeclaredField(fields.get(j).get("field"));
                        f.setAccessible(true);
                        String value = String.valueOf(f.get(obj)==null?"":f.get(obj));
                        String type = fields.get(j).get("type");
                        if("fixed".equals(type)) {
                            row.createCell((short) j + celladd).setCellValue(value+"%");
                        }else {
                            row.createCell((short) j + celladd).setCellValue(value); 
                        }
                        
                    }
            }
            FileOutputStream fout = new FileOutputStream(path+title+".xls");
            wb.write(fout);
            fout.close();                
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}