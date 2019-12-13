package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

    // 上传测试
    @Test
    public void testFastDFS(){
        // 加载配置文件
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            // 创建tracker客户端对象
            TrackerClient tracker = new TrackerClient();
            // 连接tracker
            TrackerServer trackerServer = tracker.getConnection();
            // 获取Storage
            StorageServer storeStorageServer = tracker.getStoreStorage(trackerServer);
            // 创建storageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorageServer);

            // 本地文件的路径
            String filePath = "E:\\写真\\新世纪福音战士\\58eb528010826.jpg";
            // 上传本地文件,成功后会得到文件的ID
            String fileId = storageClient1.upload_file1(filePath, "jpg", null);
            System.out.println(fileId);  // group1/M00/00/00/wKh2hF3nEnGADJ8FABQUOBifmj8025.jpg

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    // 下载测试
    @Test
    public void testDownloadFile() throws IOException, MyException {
        ClientGlobal.initByProperties("config/fastdfs-client.properties");
        TrackerClient tracker = new TrackerClient();
        TrackerServer trackerServer = tracker.getConnection();
        StorageServer storeStorageServer = tracker.getStoreStorage(trackerServer);
        StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorageServer);
        // 下载文件
        byte[] result = storageClient1.download_file1("group1/M00/00/00/wKh2hF3nEnGADJ8FABQUOBifmj8025.jpg");
        File file = new File("d:/新世纪福音战士.png");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(result);
        fileOutputStream.close();
    }


}
