package com.xuecheng.manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFile {

    // 测试文件分块
    @Test
    public void testChunk() throws IOException {
        // 源文件的路径
        File sourceFile = new File("D:\\xuecheng_video\\lucene.avi");
        // 要分成块文件的目录
        String chunkFileFolder = "D:\\xuecheng_video\\chunks\\";

        // 定义块文件的大小(1M)
        long chunkFileSize = 1 * 1024 *1024;

        // 获取源文件的块数
        // Math.ceil==>对浮点数向上取整  sourceFile.length() * 1.0 ==> 为了转换为浮点数
        long chunkFileNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkFileSize);

        // 创建读取文件的对象 r表示read
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");

        // 创建一个缓冲区
        byte[] bytes = new byte[1024];
        for (long i = 0; i < chunkFileNum; i++) {
            // 创建块文件的地址
            File file = new File(chunkFileFolder + i);
            // 向块文件中添加写入对象
            RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
            int len = -1;
            while ((len = raf_read.read(bytes)) != -1 ){

                // 开始从0开始写入,每次写入 bytes 个数据
                raf_write.write(bytes,0,len);
                // 如果块文件的大小达到1M的话,开始写下一块
                if (file.length() >= chunkFileSize){
                    break;
                }
            }
            // 关闭写入的指针
            raf_write.close();
        }
        //关闭读取的指针
        raf_read.close();
    }

    // 测试文件的合并
    @Test
    public void testMerge() throws IOException {
        // 块文件目录对象
        File chunkFolder = new File("D:\\xuecheng_video\\chunks\\");
        // 得到块文件的列表
        File[] fileArray = chunkFolder.listFiles();
        // 转成集合，对集合中的数据进行排序
        List<File> files = Arrays.asList(fileArray);
        // 从小到大排序
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                // 让文件名进行比较,比较的结果就是升序或者降序
                if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                }
                return 1;
            }
        });
        // ---------------------------------------------------------------------------
        // 合并文件对象
        File mergeFile = new File("D:\\xuecheng_video\\lucene_merge.avi");
        // 创建新的合并文件对象
        mergeFile.createNewFile();

        // 如果新文件存在的话,就删除
        if(mergeFile.exists()){
            mergeFile.delete();
        }

        // 用于向合并文件中写文件的对象
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        // 指针指向文件顶端
        raf_write.seek(0);
        // 缓冲区
        byte[] b = new byte[1024];

        // 合并文件(一块块的文件已经是排好序的)
        for(File chunkFile:files){
            // 创建读物文件的对象
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"rw");
            int len = -1;
            while((len=raf_read.read(b))!=-1){
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }
        raf_write.close();
    }

}
