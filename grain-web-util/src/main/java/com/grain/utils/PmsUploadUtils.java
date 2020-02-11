package com.grain.utils;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.multipart.MultipartFile;


public class PmsUploadUtils {


    private static String ip_address = PmsGlobalConstant.ImagAddress;

    public static String UploadFImage(MultipartFile multipartFile) {
        //上传图片到服务器
        // 配置fdfs全局连接地址
        String tracker = PmsUploadUtils.class.getResource("/tracker.conf").getPath();//获取配置文件

        try {
            ClientGlobal.init(tracker);
            TrackerClient trackerClient = new TrackerClient();
            //获得TracerServer
            TrackerServer trackerServer = null;
            trackerServer = trackerClient.getConnection();
            //通过tracker获得一个Storage连接客户端
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //获取二进制对象
            byte[] bytes = multipartFile.getBytes();
            //获得文件后缀名
            String originalFilename = multipartFile.getOriginalFilename();
            int i = originalFilename.lastIndexOf(".");
            String extName = originalFilename.substring(i + 1);
            String[] uploadInfos = storageClient.upload_file(bytes, extName, null);
            for (String uploadInfo : uploadInfos) {
                ip_address += "/" + uploadInfo;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip_address;
    }

   /* private static class Singleton{
        private static PmsUploadUtils instance;
        static {
           instance=new PmsUploadUtils();
        }
        public static  PmsUploadUtils getInstance(){
            return instance;
        }
    }
    public static PmsUploadUtils getInstance(){
        return Singleton.getInstance();
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }*/
}
