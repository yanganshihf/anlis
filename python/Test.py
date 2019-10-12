__author__ = 'Mingkof'
# coding=cp936
# Import arcpy module
import os
import stat  
import shutil
import arcpy 
import time 
import sys
reload(sys) 
def get_fileName_fileExt(filename):  
    (filepath,tempfilename) = os.path.split(filename);  
    (shotname,extension) = os.path.splitext(tempfilename);  
    return shotname
    
def get_fileName_date(filename):      
    name = get_fileName_fileExt(filename);
    name=name.split('-')[0];
    return name;

def delete_file(filePath):
    if os.path.exists(filePath):
        for fileList in os.walk(filePath):
            for name in fileList[2]:
                os.chmod(os.path.join(fileList[0],name), stat.S_IWRITE)
                os.remove(os.path.join(fileList[0],name))
        shutil.rmtree(filePath)
        return "delete ok"
    else:
        return "no filepath"
 
def xlstoarcgis(zjspath):  
 try: 
    print "1."
    field_names = [f.name for f in arcpy.ListFields(zjspath)]
    print "2."
    rows = arcpy.SearchCursor(zjspath)
    print "3."
    #SDEFilePath_SDE = r"C:\Users\Administrator\AppData\Roaming\Esri\Desktop10.3\ArcCatalog\aaa.sde" 
    SDEFilePath_SDE = r"C:\Users\Dell\AppData\Roaming\Esri\Desktop10.3\ArcCatalog\FRA.sde" 
    #SDEFilePath_SDE = r"C:\Users\zongshu\AppData\Roaming\ESRI\Desktop10.1\ArcCatalog\192.168.0.103.sde" 
    #upTime=get_fileName_date(zjspath)
    #fcspath = SDEFilePath_SDE + "\\T" + upTime
    #print ""+upTime
    fcspath = SDEFilePath_SDE + "\\diaochadian_ALL"
    print "5."+fcspath
    print "6"
    cur = arcpy.InsertCursor(fcspath)
    print "7"
    fields = arcpy.ListFields(fcspath)
    print "8"
    indexI = 1
    for row in rows:
        if row:
            print  str(indexI)
            indexI += 1
            feat = cur.newRow()
            feat.Shape = row.Shape
            print (row.global_id)
            feat.global_id = row.global_id
            feat.layer = row.layer
            feat.province = row.province
            feat.vhr_year = row.vhr_year
            feat.fra_cat_ = row.fra_cat_
            feat.frascat_1 = row.frascat_1
            feat.frascat_2 = row.frascat_2
            feat.cch_1 = row.cch_1
            feat.cch_1f = row.cch_1f
            feat.cch_2 = row.cch_2
            feat.cch_2f = row.cch_2f
            feat.h_flos_1 = row.h_flos_1
            feat.h_fga_1 = row.h_fga_1
            feat.h_stf_1 = row.h_stf_1
            feat.h_stnf_1 = row.h_stnf_1
            feat.h_flos_2 = row.h_flos_2
            feat.h_fga_2 = row.h_fga_2
            feat.h_stf_2 = row.h_stf_2
            feat.h_stnf_2 = row.h_stnf_2
            feat.lu_17_fo = row.lu_17_fo
            feat.lu_17_owl = row.lu_17_owl
            feat.lu_17_ol = row.lu_17_ol
            feat.lu_17_wa = row.lu_17_wa
            feat.frascat_3 = row.frascat_3
            feat.frascat_4 = row.frascat_4
            feat.frascat_5 = row.frascat_5
            feat.frascat_6 = row.frascat_6
            feat.frascat_7 = row.frascat_7
            cur.insertRow(feat)
    print "-10"
    del cur
    del rows 
    #filepath = os.path.dirname(zjspath)
    #delete_file(filepath);
    arcpy.Delete_management(zjspath)
    print "end" 
 except ValueError:
   print ValueError
   
def main(shpPath):
   xlstoarcgis(shpPath)
if __name__=="__main__":
    for i in range(1, len(sys.argv)):
        shpPath = sys.argv[i]
        main(shpPath) 