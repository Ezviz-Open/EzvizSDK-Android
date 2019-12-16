package com.videogo.ui.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class ZipUtil {
	private static final int BUFFER = 1024; 
	private static final String BASE_DIR = "";
	private static final String PATH = "/";
	
	private static final String SIGN_PATH_NAME = "META-INF";
	private static final String UPDATE_PATH_NAME = "\\res\\raw\\channel";
	private static final String SOURCE_PATH_NAME = "\\source\\";
	private static final String TARGET_PATH_NAME = "\\target\\";
	private static final String RESULT_PATH_NAME = "\\result\\";
	private static final String JDK_BIN_PATH = "C:\\Program Files\\Java\\jdk1.6.0_26\\bin";
	private static final String SECRET_KEY_PATH = "F:\\document\\APK\\";
	private static final String SECRET_KEY_NAME = "sdk.keystore";
	
	@SuppressWarnings("rawtypes")
	public static void unZip(String fileName, String filePath) throws Exception{  
       ZipFile zipFile = new ZipFile(fileName);
       Enumeration emu = zipFile.entries();
        
       while(emu.hasMoreElements()){  
            ZipEntry entry = (ZipEntry) emu.nextElement();  
            if (entry.isDirectory()){  
                new File(filePath+entry.getName()).mkdirs();  
                continue;  
            }  
            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));  
             
            File file = new File(filePath + entry.getName());  
            File parent = file.getParentFile();  
            if(parent != null && (!parent.exists())){  
                parent.mkdirs();  
            }  
            FileOutputStream fos = new FileOutputStream(file);  
            BufferedOutputStream bos = new BufferedOutputStream(fos,BUFFER);  
      
            byte [] buf = new byte[BUFFER];  
            int len = 0;  
            while((len=bis.read(buf,0,BUFFER))!=-1){  
                fos.write(buf,0,len);  
            }  
            bos.flush();  
            bos.close();  
            bis.close();  
           }  
           zipFile.close();  
    }  
    
    public static void unZipGetLib(String fileName, String filePath) throws Exception{  
        ZipFile zipFile = new ZipFile(fileName);
        Enumeration emu = zipFile.entries();
         
        while(emu.hasMoreElements()){  
             ZipEntry entry = (ZipEntry) emu.nextElement();  
             if (entry.isDirectory()){   
                 continue;  
             }  
             String entryName = entry.getName();
             Pattern p = Pattern.compile(".*(SO|so)$"); //匹配so后缀的文件  
             Matcher m = p.matcher(entryName);
             if(!m.matches()) {
                 continue; 
             }
             BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));  
              
             File file = new File(filePath + "/" + entry.getName());  
             File parent = file.getParentFile();  
             if(parent != null && (!parent.exists())){  
                 parent.mkdirs();  
             }  
             FileOutputStream fos = new FileOutputStream(file);  
             BufferedOutputStream bos = new BufferedOutputStream(fos,BUFFER);  
       
             byte [] buf = new byte[BUFFER];  
             int len = 0;  
             while((len=bis.read(buf,0,BUFFER))!=-1){  
                 fos.write(buf,0,len);  
             }  
             bos.flush();  
             bos.close();  
             bis.close();  
            }  
            zipFile.close();  
     }  
    
	public static void compress(String srcFile, String destPath) throws Exception {
		compress(new File(srcFile), new File(destPath));
	}
	
	public static void compress(File srcFile, File destFile) throws Exception {
		// 对输出文件做CRC32校验
		CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(
				destFile), new CRC32());

		ZipOutputStream zos = new ZipOutputStream(cos);
		compress(srcFile, zos, BASE_DIR);

		zos.flush();
		zos.close();
	}
	
	private static void compress(File srcFile, ZipOutputStream zos,
			String basePath) throws Exception {
		if (srcFile.isDirectory()) {
			compressDir(srcFile, zos, basePath);
		} else {
			compressFile(srcFile, zos, basePath);
		}
	}
	
	private static void compressDir(File dir, ZipOutputStream zos,
			String basePath) throws Exception {
		File[] files = dir.listFiles();
		// 构建空目录
		if (files.length < 1) {
			ZipEntry entry = new ZipEntry(basePath + dir.getName() + PATH);

			zos.putNextEntry(entry);
			zos.closeEntry();
		}
		
		String dirName = "";
		String path = "";
		for (File file : files) {
			//当父文件包名为空时，则不把包名添加至路径中（主要是解决压缩时会把父目录文件也打包进去）
			if(basePath!=null && !"".equals(basePath)){
				dirName=dir.getName(); 
			}
			path = basePath + dirName + PATH;
			// 递归压缩
			compress(file, zos, path);
		}
	}

	private static void compressFile(File file, ZipOutputStream zos, String dir)
			throws Exception {
		/**
		 * 压缩包内文件名定义
		 * 
		 * <pre>
		 * 如果有多级目录，那么这里就需要给出包含目录的文件名
		 * 如果用WinRAR打开压缩包，中文名将显示为乱码
		 * </pre>
		 */
		if("/".equals(dir))dir="";
		else if(dir.startsWith("/"))dir=dir.substring(1,dir.length());
		
		ZipEntry entry = new ZipEntry(dir + file.getName());
		zos.putNextEntry(entry);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		int count;
		byte data[] = new byte[BUFFER];
		while ((count = bis.read(data, 0, BUFFER)) != -1) {
			zos.write(data, 0, count);
		}
		bis.close();

		zos.closeEntry();
	}
	
	public static void main(String[] args)throws Exception{
		StringBuffer buffer = new StringBuffer();
		BufferedReader br =null;
		OutputStreamWriter osw =null;
		String srcPath = "F:\\document\\APK\\new\\iGouShop.apk";
		String channelCode = "channel_id=LD20120926";
		
		File srcFile = new File(srcPath);
		String parentPath = srcFile.getParent();	//源文件目录
		String fileName = srcFile.getName();		//源文件名称
		String prefixName = fileName.substring(0, fileName.lastIndexOf("."));
		//解压源文件保存路径
		String sourcePath = buffer.append(parentPath).append(SOURCE_PATH_NAME).
								append(prefixName).append("\\").toString();
		
		//------解压
		unZip(srcPath, sourcePath);
		
		//------删除解压后的签名文件
		String signPathName = sourcePath+SIGN_PATH_NAME;
		File signFile = new File(signPathName);
		if(signFile.exists()){
			File sonFiles[] = signFile.listFiles();
			if(sonFiles!=null && sonFiles.length>0){
				//循环删除签名目录下的文件
				for(File f : sonFiles){
					f.delete();
				}
			}
			signFile.delete();
		}
		
		//------修改渠道号
		buffer.setLength(0);
		String path = buffer.append(parentPath).append(SOURCE_PATH_NAME)
				.append(prefixName).append(UPDATE_PATH_NAME).toString();
		br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		while((br.readLine())!=null)
		{
			osw = new OutputStreamWriter(new FileOutputStream(path));  
			osw.write(channelCode,0,channelCode.length());  
			osw.flush();  
		}
		
		//------打包
		String targetPath = parentPath+TARGET_PATH_NAME;
		//判断创建文件夹
		File targetFile = new File(targetPath);
		if(!targetFile.exists()){
			targetFile.mkdir();
		}
		compress(parentPath+SOURCE_PATH_NAME+prefixName,targetPath+fileName);
		
		//------签名
		File ff =new File(JDK_BIN_PATH);
		String resultPath = parentPath+RESULT_PATH_NAME;
		//判断创建文件夹
		File resultFile = new File(resultPath);
		if(!resultFile.exists()){
			resultFile.mkdir();
		}
		
		//组合签名命令
		buffer.setLength(0);
		buffer.append("cmd.exe /c jarsigner -keystore ")
		.append(SECRET_KEY_PATH).append(SECRET_KEY_NAME)
		.append(" -storepass winadsdk -signedjar ")
		.append(resultPath).append(fileName).append(" ")	//签名保存路径应用名称
		.append(targetPath).append(fileName).append(" ")	//打包保存路径应用名称
		.append(SECRET_KEY_NAME);
		//利用命令调用JDK工具命令进行签名
		Process process = Runtime.getRuntime().exec(buffer.toString(),null,ff);
		if(process.waitFor()!=0)System.out.println("文件打包失败！！！");
	}
}
