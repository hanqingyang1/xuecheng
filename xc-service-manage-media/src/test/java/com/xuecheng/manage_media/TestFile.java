package com.xuecheng.manage_media;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @version 1.0
 **/
public class TestFile {


    @Test
    public void chunk() throws IOException {
        //源文件
        File sourceFile = new File("G:\\static\\video\\source\\lucene.avi");
        //块目录
        String chunkFileFolder = "G:\\static\\video\\chunkFolder\\chunk\\";
        //先定义块文件大小
        long chunkFilesize = 1*1024*1024;
        //计算块文件数量
        long chunkFileNum = (long) Math.ceil(sourceFile.length()*1.0 /chunkFilesize);
        //创建读文件对象
        RandomAccessFile ref_read = new RandomAccessFile(sourceFile,"r");

        //创建字节对象
        byte[] b = new byte[1024];
        for (int i= 0;i < chunkFileNum; i++){

            //写出位置
            File chunkFile = new File(chunkFileFolder+i);
            RandomAccessFile ref_write = new RandomAccessFile(chunkFile,"rw");
            int len = -1;
            while((len = ref_read.read(b)) != -1){
                ref_write.write(b,0,len);
                if(chunkFile.length()>=chunkFilesize){
                    break;
                }
            }
            ref_write.close();
        }

        ref_read.close();

    }

    @Test
    public void testMergeFile() throws IOException {
        String chunkSourceFolderPath = "G:\\static\\video\\chunkFolder\\chunk\\";

        File chunkSourceFolder = new File(chunkSourceFolderPath);

        File[] files = chunkSourceFolder.listFiles();
        List<File> fileList = Arrays.asList(files);

        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;

            }
        });

        File mergeFile = new File("G:\\static\\video\\source\\lucene_merge.avi");
        boolean newFile = mergeFile.createNewFile();

        RandomAccessFile ref_write = new RandomAccessFile(mergeFile,"rw");

        byte[] b = new byte[1024];
        for (File file : fileList) {

            RandomAccessFile ref_read = new RandomAccessFile(file,"r");
            int len = 0;
            while((len = ref_read.read(b)) != -1){
                ref_write.write(b,0,len);
            }
            ref_read.close();

        }
        ref_write.close();

    }















  /*  //测试文件分块
    @Test
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("E:\\ffmpeg_test\\lucene.avi");
        //块文件目录
        String chunkFileFolder = "E:\\ffmpeg_test\\chunks\\";

        //先定义块文件大小
        long chunkFileSize = 1 * 1024 * 1024;

        //块数
        long chunkFileNum = (long) Math.ceil(sourceFile.length() * 1.0 /chunkFileSize);

        //创建读文件的对象
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile,"r");

        //缓冲区
        byte[] b = new byte[1024];
        for(int i=0;i<chunkFileNum;i++){
            //块文件
            File chunkFile = new File(chunkFileFolder+i);
            //创建向块文件的写对象
            RandomAccessFile raf_write = new RandomAccessFile(chunkFile,"rw");
            int len = -1;

            while((len = raf_read.read(b))!=-1){

                raf_write.write(b,0,len);
                //如果块文件的大小达到 1M开始写下一块儿
                if(chunkFile.length()>=chunkFileSize){
                    break;
                }
            }
            raf_write.close();


        }
        raf_read.close();
    }


    //测试文件合并
    @Test
    public void testMergeFile() throws IOException {
        //块文件目录
        String chunkFileFolderPath = "E:\\ffmpeg_test\\chunks\\";
        //块文件目录对象
        File chunkFileFolder = new File(chunkFileFolderPath);
        //块文件列表
        File[] files = chunkFileFolder.listFiles();
        //将块文件排序，按名称升序
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;

            }
        });

        //合并文件
        File mergeFile = new File("E:\\ffmpeg_test\\lucene_merge.avi");
        //创建新文件
        boolean newFile = mergeFile.createNewFile();

        //创建写对象
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");

        byte[] b = new byte[1024];
        for(File chunkFile:fileList){
            //创建一个读块文件的对象
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
            int len = -1;
            while((len = raf_read.read(b))!=-1){
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }
        raf_write.close();
    }*/
}
