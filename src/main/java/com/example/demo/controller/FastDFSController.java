package com.example.demo.controller;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class FastDFSController {

    @Autowired
    private FastFileStorageClient client;

    private ConcurrentHashMap<Integer,String> map=new ConcurrentHashMap<>();

    @RequestMapping("/upload")
    public String upload(@RequestParam MultipartFile file){
        StorePath storePath=null;
        try{
            storePath=client.uploadFile(file.getInputStream(),file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()),null);

            map.put(2,file.getOriginalFilename());
        }catch (Exception e){
            e.printStackTrace();
        }

        assert storePath != null;
        map.put(1,storePath.getFullPath());
        return storePath.getFullPath();
    }

    @RequestMapping("/delete")
    public String delete(){
        String s=map.get(1);
        client.deleteFile(s);

        return s+"  "+"被删除了";
    }

    @RequestMapping("/get")
    public void get(HttpServletResponse response){
        StorePath storePath=StorePath.parseFromUrl(map.get(1));

        String group=storePath.getGroup();
        String path=storePath.getPath();

        byte[] bytes=client.downloadFile(group,path,new DownloadByteArray());

        try{
            response.reset();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(map.get(2),"utf-8"));

            OutputStream outputStream=response.getOutputStream();
            outputStream.write(bytes);
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
