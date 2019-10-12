package com.integration.action;

import java.awt.Point;
import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.integration.entity.FeiXing;
import com.integration.entity.Heshi;
import com.integration.entity.JqgridPageResp;
import com.integration.entity.PDFFile;
import com.integration.entity.SysUser;
import com.integration.service.FeiXingService;
import com.integration.service.OperationLogService;
import com.integration.service.PDFFileService;
import com.integration.util.ZipUtil;



@Controller
@RequestMapping("file/")
public class FileAction {
	
	@Autowired
	PDFFileService ps;
	@Autowired
	OperationLogService os;
	@Autowired
	FeiXingService fxs;

	@RequestMapping(value = "ZipUpload", method = RequestMethod.POST)
	@ResponseBody
	public Object ZipUpload(String name,MultipartFile file,HttpServletRequest request)  {
		try {
			// 上传到临时文件夹下
			File zipfile = castFile(file,request);
			
			//zip上传成功,开始解压
			// String unzipFilePath = request.getSession().getServletContext().getRealPath("/")+"uploadPoliceFiles/unzip";
			 String unzipFilePath =
						request.getSession().getServletContext().getRealPath("/")+"uploadPoliceFiles\\unzip";
	         ZipUtil.unzip(zipfile.getAbsolutePath(), unzipFilePath, false); 
	         
	         // 获取shp文件路径
	         String url=unzipFilePath+"\\"+ name.substring(0,name.lastIndexOf("."))+".shp";
	         System.out.println(url);
	         // 获取python路径
	         String pythonFilePath =
						request.getSession().getServletContext().getRealPath("/")+"python\\Test.py";
	         System.out.println(pythonFilePath);
	         // 执行python脚本
	         System.out.println("start;"+url);
	         // String[] args1 = new String[] { "python", pythonFilePath, url}; 
	         String[] args1 = new String[] { "python", pythonFilePath,url}; 
	         Process pr=Runtime.getRuntime().exec(args1);
	         BufferedReader in = new BufferedReader(new InputStreamReader(
	           pr.getInputStream()));
	         String line;
	         while ((line = in.readLine()) != null) {
	          System.out.println(line);
	         }
	         in.close();
	         pr.waitFor();
	         System.out.println("end");
	         zipfile.delete();
	         Map<String,String> result = new HashMap<>();
	         result.put("code", "上传成功!");
	 		 return result;
		} catch (Exception e) {
			// TODO: handle exception
			return "";
		}
		
	}
	
	
	
	@RequestMapping(value = "ExcUpload", method = RequestMethod.POST)
	@ResponseBody
	public Object ExcUpload(String name,MultipartFile file,HttpServletRequest request)  {
		try {
			
			// 上传到临时文件夹下
			File excfile = castFile(file,request);
			
			// System.out.println(excfile.getAbsolutePath());
	        // 读取表数据
			//1、获取文件输入流
			InputStream inputStream = new FileInputStream(excfile.getAbsolutePath());
			//2、获取Excel工作簿对象
			 @SuppressWarnings("resource")
			HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
			 //3、得到Excel工作表对象
			HSSFSheet sheetAt = workbook.getSheetAt(0);
			//4、循环读取表格数据
			Heshi heshi = new Heshi();
			for (Row row : sheetAt) {
				//首行（即表头）不读取
				if (row.getRowNum() == 0) {
					continue;
				}
				
				Cell cell = row.getCell(2);
				
				BigDecimal bd = new BigDecimal(cell.toString());
				String str2 = bd.toString();
				if (str2.indexOf("E+8") != -1) {
					str2 = str2.replace(".", "").replace("E+8", "0");
				}
				
				// 封装对象
				heshi.setSheng(row.getCell(0).getStringCellValue());
				heshi.setXian(row.getCell(1).getStringCellValue());
				heshi.setYangdihao(str2);
				
				heshi.setFx_pd(row.getCell(3).getStringCellValue());
				heshi.setFx_hs(row.getCell(4).getStringCellValue());
				heshi.setFx_wp(row.getCell(5).getStringCellValue());
				
				heshi.setLbx_pd(row.getCell(6).getStringCellValue());
				heshi.setLbx_hs(row.getCell(7).getStringCellValue());
				heshi.setLbx_wp(row.getCell(8).getStringCellValue());
				
				heshi.setRenyuan(row.getCell(9).getStringCellValue());
				heshi.setBeizhu(row.getCell(10).getStringCellValue());
				
				fxs.addHeshi(heshi);
			}
			
	         Map<String,String> result = new HashMap<>();
	         result.put("code", "上传成功!");
	         // 删除缓存文件
	         excfile.delete();
	 		 return result;
		} catch (Exception e) {
			// TODO: handle exception
			return "";
		}
		
	}
	
	

	@RequestMapping(value = "ExcUpload2", method = RequestMethod.POST)
	@ResponseBody
	public Object ExcUpload2(String name,MultipartFile file,HttpServletRequest request)  {
		try {
			
			// 上传到临时文件夹下
			File excfile = castFile(file,request);
			
			// System.out.println(excfile.getAbsolutePath());
	        // 读取表数据
			//1、获取文件输入流
			InputStream inputStream = new FileInputStream(excfile.getAbsolutePath());
			//2、获取Excel工作簿对象
			 @SuppressWarnings("resource")
			HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
			 //3、得到Excel工作表对象
			HSSFSheet sheetAt = workbook.getSheetAt(0);
			//4、循环读取表格数据
			FeiXing feiXing = new FeiXing();
			for (Row row : sheetAt) {
				//首行（即表头）不读取
				if (row.getRowNum() == 0) {
					continue;
				}
				
				Cell cell = row.getCell(1);
				
				BigDecimal bd = new BigDecimal(cell.toString());
				String str2 = bd.toString();
				if (str2.indexOf("E+8") != -1) {
					str2 = str2.replace(".", "").replace("E+8", "0");
				}
				
				// 封装对象
				feiXing.setSheng(row.getCell(0).getStringCellValue());
				feiXing.setYangdihao(str2);
				feiXing.setJiashiyuan(row.getCell(2).getStringCellValue());
				feiXing.setFujiashiyuan(row.getCell(3).getStringCellValue());
				
				fxs.addFeiXing(feiXing);
			}
			
	         Map<String,String> result = new HashMap<>();
	         result.put("code", "上传成功!");
	         // 删除缓存文件
	         excfile.delete();
	 		 return result;
		} catch (Exception e) {
			// TODO: handle exception
			return "";
		}
		
	}
	
	/**
	  * 上传dbf文件
	 * 
	 * @param file_obj
	 * @param request
	 * @return
	 * @throws Exception
	 * @author yanganshi
	 * @time 2019-6-3 13:34:32
	 */
	@SuppressWarnings("unused")
	@RequestMapping(value = "shpfiles", method = RequestMethod.POST)
	@ResponseBody
	public Object shpfiles(String dataType,String importType,MultipartFile dbfFile,MultipartFile prjFile,MultipartFile shpFile,MultipartFile shxFile, HttpServletRequest request) throws Exception {
/*		System.out.println(dataType+","+importType);
		
		// 转换成可用类型
		File dbf = castFile(dbfFile,request);
		File prj = castFile(prjFile,request);
		File shp = castFile(shpFile,request);
		File shx = castFile(shxFile,request);
		
		// 读取shp文件中的参数
		String absolutePathShp = shp.getAbsolutePath();
		// 返回shapelist
		ArrayList<String> readShp = readShp(absolutePathShp);
				
		
		
		// 读取dbf文件中的参数
		String absolutePathDbf = dbf.getAbsolutePath();
		ArrayList<Map> readDbf = readDbf(absolutePathDbf,readShp);
		
		
		
		// 删除临时文件
		dbf.delete();
		prj.delete();
		shp.delete();
		shx.delete();*/
        Map<String,String> result = new HashMap<>();
        result.put("code", "上传成功!");
		return result;
	}
	
	
	/**
	 * multipartFile 转换file 方法
	 * 
	 * @param multipartFile
	 * @param request
	 * @return
	 * @author yanganshi
	 * @time 2019-6-3 15:32:24
	 */
	public File castFile(MultipartFile multipartFile, HttpServletRequest request) {
		File file = null;
		
		if(!multipartFile.isEmpty()) {
			String filePath = 
					request.getSession().getServletContext().getRealPath("/")+"uploadPoliceFiles/"+multipartFile.getOriginalFilename();
			try {
				multipartFile.transferTo(new File(filePath));
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			file = new File(filePath);	
			
		}
		return file;
	}
	
	
	
	/**
	  * 上传dbf文件
	 * 
	 * @param file_obj
	 * @param request
	 * @return
	 * @throws Exception
	 * @author yanganshi
	 * @time 2019-6-3 13:34:32
	 */
	/*@RequestMapping(value = "dbf", method = RequestMethod.POST)
	@ResponseBody
	public Object dbf(MultipartFile file_obj, HttpServletRequest request) throws Exception {
		
		DbaseFileReader reader = null;
		
		// 用于存储属性集合容器
		ArrayList<Map> arrayList = new ArrayList<Map>();
		
		// 把文件放入缓存位置
		if(!file_obj.isEmpty()) {
			String filePath = 
			request.getSession().getServletContext().getRealPath("/")+"uploadPoliceFiles/"+file_obj.getOriginalFilename();
			
			file_obj.transferTo(new File(filePath));
			File file = new File(filePath);
		
			
			ArrayList<Map> readDbf = readDbf(filePath);
			// 用完临时文件后删除 
			reader.close();
			file.delete();
		}
		
        return "";
	}
	*/
	
	
	
	/**
	  * 读取dbf文件
	 * 
	 * @param filePath
	 * @return 参数集合
	 * @throws IOException
	 * @author yanganshi
	 * @throws SQLException 
	 * @time 2019-6-3 13:34:32
	 */
	public ArrayList<Map> readDbf(String filePath,ArrayList<String> readShp) throws IOException, SQLException {
		DbaseFileReader reader = null;
		// 用于存储属性集合容器
		ArrayList<Map> arrayList = new ArrayList<Map>();
		// 获取文件流
		File file = new File(filePath);
		try {
			// 获得shpfile对象
			ShpFiles shpFiles = new ShpFiles(file);
			// 获得reder
			reader = new DbaseFileReader(shpFiles, false, Charset.forName("GBK"));
			// 读取文件头
			DbaseFileHeader header = reader.getHeader();
			// 获取参数个数
			int numFields = header.getNumFields();
			
			int count = 0;
			while(reader.hasNext()) {
				StringBuffer sql = new StringBuffer();
				sql.append("insert INTO pandu ");
				sql.append("(Shape,global_id, layer, province_c, province,vhr_year,fra_cat_,frascat_1,frascat_2,sure_1,cch_1,cch_1f,cch_2,cch_2f,sure_2,h_flos_1,h_fga_1,h_stf_1,h_stnf_1,h_flos_2,h_fga_2,h_stf_2,h_stnf_2,lu_17_fo,lu_17_owl,lu_17_ol,lu_17_wa,sure_3,frascat_3,frascat_4,frascat_5,frascat_6,frascat_7,plot_id) ");
				sql.append("VALUES ");
				String params = "";
				Object[] entry = reader.readEntry();
				Map fieldcontent = new HashMap();
				for (int i = 0; i < numFields; i++) {
					// 读取字段名
					String title = header.getFieldName(i);
					// 获取字段值
					Object value = entry[i];
					// 字段值添加到map对象中
					// fieldcontent.put(title, value);
					// System.out.println(title +" : "+value);
					if(i==0) {
						if(value == null) {
							params = "("+value+",";
						}else {
							// 如果是数字类型的
							if(value.toString().indexOf(".") != -1 || title == "plot_id") {
								
								params = ","+value+",";
							}else {
								params = ","+"'"+value+"'"+",";
							}
						}
						
						
					}else if(i == (numFields-1)) {
						if(value == null) {
							params += value+")";
						}else {
							if(value.toString().indexOf(".") != -1 || title == "plot_id") {
								params += value+")";
							}else {
								params += "'"+value+"'"+")";
							}
						}
						
					}else {
						if(value == null) {
							params += value+",";
						}else {
							if(  value.toString().indexOf(".") != -1 || title == "plot_id") {
								params += value+",";
							}else {
								params += "'"+value+"'"+",";
							}
						}
						
					}
				}
				params = params.replace(",,," ,  ",'','',");
				params = params.replace(",," ,  ",'',");
				params = "("+"'"+readShp.get(count)+"'"+params;
				sql.append(params);
				// System.out.println(params);
				// System.out.println(readShp.get(count));
				count++;
				// 对象加入到结果集中
				// arrayList.add(fieldcontent);
				System.out.println(sql.toString());
				
				
				
				/**
				 * 插入数据库
				 */
				String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver"; //加载JDBC驱动 
				String dbURL = "jdbc:sqlserver://192.168.1.115:1433;DatabaseName = ResourceMonitor"; //连接服务器和数据库sample 
				String userName = "sa"; //默认用户名 
				String userPwd = "RTSrts1234"; //密码 
				PreparedStatement pstmt = null;
				Connection conn = null;
				
				try {
					Class.forName(driverName);
					conn = DriverManager.getConnection(dbURL, userName, userPwd); 
					pstmt = conn.prepareStatement(sql.toString());
					pstmt.executeUpdate();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					pstmt.close();
					conn.close();
				}
				
			}
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 关闭流
		reader.close();
		return arrayList;
	}
	
	
	
	/**
	  * 上传shp文件
	 * 
	 * @param file_obj
	 * @param request
	 * @return
	 * @throws Exception
	 * @throws IOException
	 * @author yanganshi
	 * @time 2019-6-3 13:42:47
	 */
	/*@RequestMapping(value = "shp", method = RequestMethod.POST)
	@ResponseBody
	public Object shp(MultipartFile  file_obj, HttpServletRequest request) throws Exception, IOException {
		// 把文件放入缓存位置
		if(!file_obj.isEmpty()) {
			String filePath = 
			request.getSession().getServletContext().getRealPath("/")+"uploadPoliceFiles/"+file_obj.getOriginalFilename();
			System.out.println(filePath);
			file_obj.transferTo(new File(filePath));
			File file = new File(filePath);
			
			readShp(filePath);
			
			
			//用完临时文件后删除 
			file.delete();
		}
		
        return "";
	}*/
	
	static ShapefileDataStore shpDataStore = null;
	
	/**
	  * 读取shp文件参数方法
	 * 
	 * @param filePath
	 * @author yanganshi
	 * @return 
	 * @time 2019-6-3 13:42:33
	 */
	public ArrayList<String> readShp(String filePath) {
		ArrayList<String> shapeList = new ArrayList<String>();
		// 获取文件流对象
		File file = new File(filePath);
		try {
			// 获取shp相关参数
			URL url = file.toURI().toURL();
			// 设置字符集
			shpDataStore = new ShapefileDataStore(url);
			shpDataStore.setCharset(Charset.forName("GBK"));

			// 获取数据源类型名称
			String typeName = shpDataStore.getTypeNames()[0];
			SimpleFeatureType schema = shpDataStore.getSchema(typeName);

			String wkid = "0";
			CoordinateReferenceSystem coordinateReferenceSystem = schema.getCoordinateReferenceSystem();
			try {
				Integer lookupEpsgCode = CRS.lookupEpsgCode(coordinateReferenceSystem, true);

				wkid = "" + lookupEpsgCode;
			} catch (FactoryException e) {
				// wkid为unknown 时会出错
				e.printStackTrace();
				wkid = "0";
			}
			// System.out.println(wkid);

			// 获取feature图层
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = null;
			featureSource = shpDataStore.getFeatureSource(typeName);
			FeatureCollection<SimpleFeatureType, SimpleFeature> result = featureSource.getFeatures();
			// System.out.println(result.size());
			// 获得feature集合
			FeatureIterator<SimpleFeature> itertor = result.features();
			// 迭代遍历
			while (itertor.hasNext()) {
				SimpleFeature feature = itertor.next();
				Collection<Property> properties = feature.getProperties();
				Iterator<Property> it = properties.iterator();

				String geometry = "";

				while (it.hasNext()) {
					// 获取所有features的参数
					Property pro = it.next();
					
					
					if (pro.getValue() instanceof Point) {
						// 点
						System.out.println(pro.getValue().toString());
					} else if (pro.getValue() instanceof LineString) {
						// 线
						System.out.println(pro.getValue().toString());
					} else if (pro.getValue() instanceof Polygon) {
						// 面
						System.out.println(pro.getValue().toString());
					} else if (pro.getValue() instanceof MultiPolygon) {
						String Shape =  ((MultiPolygon)pro.getValue()).toString();
						Shape = Shape.replace("MULTIPOLYGON", "POLYGON");
						Shape = Shape.replace("(((", "((");
						Shape = Shape.replace(")))", "))");
						shapeList.add(Shape);
						// 多面
						// System.out.println(Shape);
						
					}
				}
			}
			
			

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return shapeList;
	}
	
	
	/**
	  * 条件查询位置
	 * 
	 * @param department
	 * @param mobile
	 * @param personName
	 * @return
	 */
	@RequestMapping(value = "listfile", method = RequestMethod.POST)
	@ResponseBody
	public Object queryLog(Integer page, Integer rows, String fileName, String fileNum,Integer fileType) {
		JqgridPageResp<PDFFile> resp = new JqgridPageResp<PDFFile>();
		// 验证参数
		fileName = checkParam(fileName);
		fileNum = checkParam(fileNum);
		fileName = blur(fileName);
		fileNum = blur(fileNum);
		
		PageHelper.offsetPage((page - 1) * rows, rows);
		List<PDFFile> listEvents = ps.find_File_List(fileName, fileNum,fileType);

		// 记录数
		int records = (int) new PageInfo<>(listEvents).getTotal();
		// 总页数
		int total = 0;
		if (records % rows == 0) {
			total = records / rows;
		} else {
			total = records / rows + 1;
		}
		resp.setRecords(records);
		resp.setTotal(total);
		resp.setRows(listEvents);
		return resp;
	}
	
	/**
	 * 删除文件
	 * 
	 * @param department
	 * @param mobile
	 * @param personName
	 * @return
	 */
	@RequestMapping(value = "delFile", method = RequestMethod.POST)
	@ResponseBody
	public Object delFile(HttpServletRequest request,String id,String fileType,String fileName) {
		JqgridPageResp<PDFFile> resp = new JqgridPageResp<PDFFile>();
		ps.delFile(id,fileType);
		String path = "E:/uploadPoliceFiles/" + fileName;
		File file = new File(path);
        if(file.exists()){
            file.delete();
        }
	    Subject subject = SecurityUtils.getSubject();
	    SysUser user = (SysUser) subject.getPrincipal();
		String logUser = user.getUserName();	
		String logMobile = user.getMobile();
		String ModuleType = "上传下达";
		String OperationType = "删除";
		String OperationInfo;
		String Logid = UUID.randomUUID().toString().replaceAll("-", "");
		Date now = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String logTime = simpleDateFormat.format(now);
		switch (fileType) {
		case "0":
			OperationInfo = logUser + " 删除了  一条政策法规";
			break;
		default:
			 OperationInfo = logUser + " 删除了  一条公示监督";
			break;
		}
		os.addInfo(Logid,ModuleType,OperationType,logUser,logMobile,logTime,OperationInfo);
		resp.setMsg("删除成功");
		return resp;
		 /* File file = new File(filePath);
          if(file.exists()){
              flag=file.delete();
          }*/
	}
	
	
	@RequestMapping(value = "newFileInsert", method = RequestMethod.POST)
	@ResponseBody
	public Object newFileInsert(String fileName,String fileCode,Integer fileType,String uploader,String uploadTime) {
		JqgridPageResp<PDFFile> resp = new JqgridPageResp<PDFFile>();
		// 生成id
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		// 验证参数
		try {
			ps.newFileInsert(fileName,fileCode,fileType,uploader,uploadTime,uuid);
			resp.setMsg("上传成功");
		} catch (Exception e) {
			// TODO: handle exception
			resp.setMsg("上传失败,请重试");
		}
		
		return resp;
	}
	
	/*
	 * 采用spring提供的上传文件的方法
	 */
	@RequestMapping("springUpload")
	@ResponseBody
	public Object springUpload(HttpServletRequest request) throws IllegalStateException, IOException

	{	
		JqgridPageResp<PDFFile> resp = new JqgridPageResp<PDFFile>();
		// long startTime = System.currentTimeMillis();
		// 将当前上下文初始化给 CommonsMutipartResolver （多部分解析器）
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		// 检查form中是否有enctype="multipart/form-data"
		if (multipartResolver.isMultipart(request)) {
			// 将request变成多部分request
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			// 获取multiRequest 中所有的文件名
			@SuppressWarnings("rawtypes")
			Iterator iter = multiRequest.getFileNames();

			while (iter.hasNext()) {
				// 一次遍历所有文件
				MultipartFile file = multiRequest.getFile(iter.next().toString());
				if (file != null) {
					String path = "E:/uploadPoliceFiles" + file.getOriginalFilename();
					// 上传
					file.transferTo(new File(path));
				}

			}

		}
		resp.setMsg("上传成功");
		/*long endTime = System.currentTimeMillis();
		System.out.println("方法三的运行时间：" + String.valueOf(endTime - startTime) + "ms");*/
		return resp;
	}

	
	/*
	 * 采用spring提供的上传文件的方法
	 */
	@RequestMapping("UploadExecl")
	@ResponseBody
	public Object UploadExecl(HttpServletRequest request) throws IllegalStateException, IOException

	{	
		JqgridPageResp<PDFFile> resp = new JqgridPageResp<PDFFile>();
		List<String>  data= new ArrayList<>();
		String message="";
	    Integer code = 0;
		// long startTime = System.currentTimeMillis();
		// 将当前上下文初始化给 CommonsMutipartResolver （多部分解析器）
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		// 检查form中是否有enctype="multipart/form-data"
		if (multipartResolver.isMultipart(request)) {
			// 将request变成多部分request
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			// 获取multiRequest 中所有的文件名
			@SuppressWarnings("rawtypes")
			Iterator iter = multiRequest.getFileNames();

			
			MultipartFile file = multiRequest.getFile(iter.next().toString());
			InputStream is = file.getInputStream(); 
			HSSFWorkbook book = new HSSFWorkbook(is);
		    HSSFSheet sheet = book.getSheetAt(0);
		    HSSFRow row = null;
            Cell cell=null;
            row = sheet.getRow(0);
            if(row!=null) {
                String BH="",JZ="",X="",Y="",H="";
                cell = row.getCell(0);
                if(cell != null){                                                // 再次判断,只有cell 不为 null时,再给no赋值,否则,no="";这样就避免了空指针.
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    BH=cell.getStringCellValue();                             
                }
                cell = row.getCell(1);
                if(cell != null){
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    JZ=cell.getStringCellValue(); 
                }
                cell = row.getCell(2);
                if(cell != null){
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    X=cell.getStringCellValue(); 
                }
                cell = row.getCell(3);
                if(cell != null){
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    Y=cell.getStringCellValue(); 
                }
                cell = row.getCell(4);
                if(cell != null){
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    H=cell.getStringCellValue(); 
                }
                if(!"BH".equals(BH.trim())) {
                    message="BH字段必须在第一列!";
                    code = 1;
                }else if(!"JZ".equals(JZ.trim())){
                    message="JZ字段必须在第二列!";
                    code = 1; 
                }else if(!"X".equals(X.trim())){
                    message="X字段必须在第三列!";
                    code = 1;
                }else if(!"Y".equals(Y.trim())){
                    message="Y字段必须在第列!";
                    code = 1;
                }else if(!"H".equals(H.trim())){
                    message="H字段出错!";
                    code = 1;
                }
            }
            if(code!=1) {
            	int totalRows = sheet.getLastRowNum();
    		    for (int i = 1; i <= totalRows; i++) {
    		    	String x="",y="";
    		    	 row = sheet.getRow(i);
    		    	 cell = row.getCell(2);
    		    	 if(cell != null){
    	                  cell.setCellType(Cell.CELL_TYPE_STRING);
    	                  x=cell.getStringCellValue(); 
    	             }
    		    	 cell = row.getCell(3);
    	             if(cell != null){
    	                 cell.setCellType(Cell.CELL_TYPE_STRING);
    	                 y=row.getCell(3).getStringCellValue();
    	             }
    	             data.add(x+","+y);  
    		    }
            }
		}
		resp.setMsg(message);
		resp.setStatus(code);
		resp.setRows(data);
		/*long endTime = System.currentTimeMillis();
		System.out.println("方法三的运行时间：" + String.valueOf(endTime - startTime) + "ms");*/
		return resp;
	}
	
	
	
	/*
	 * 采用spring提供的上传文件的方法
	 */
	@RequestMapping("upload_identifyPhoto")
	@ResponseBody
	public Object upload_identifyPhoto(HttpServletRequest request,String newName) throws IllegalStateException, IOException

	{	
		JqgridPageResp<PDFFile> resp = new JqgridPageResp<PDFFile>();
		// long startTime = System.currentTimeMillis();
		// 将当前上下文初始化给 CommonsMutipartResolver （多部分解析器）
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		// 检查form中是否有enctype="multipart/form-data"
		if (multipartResolver.isMultipart(request)) {
			// 将request变成多部分request
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			// 获取multiRequest 中所有的文件名
			@SuppressWarnings("rawtypes")
			Iterator iter = multiRequest.getFileNames();

			while (iter.hasNext()) {
				// 一次遍历所有文件
				MultipartFile file = multiRequest.getFile(iter.next().toString());
				if (file != null) {
					String path = "E:/IdentifyPhoto/" + newName;
					// 上传 
					file.transferTo(new File(path));
				}

			}

		}
		resp.setMsg("上传成功");
		/*long endTime = System.currentTimeMillis();
		System.out.println("方法三的运行时间：" + String.valueOf(endTime - startTime) + "ms");*/
		return resp;
	}
	
	
	/**
	 * 校验参数
	 * 
	 * @param param
	 * @return
	 */
	public String checkParam(String param) {
		// 如果参数为空 则跳过该参数
		if (param == null || param.length() <= 0 || "全部".equals(param)) {
			param = "%";
		}
		return param;
	}

	/**
	 * 模糊查询参数过滤
	 * 
	 * @param param
	 * @return
	 */
	public String blur(String param) {
		param = param + '%';

		return param;
	}

}
