package com.xunli.filesystem.web;

import com.xunli.filesystem.model.AppVersionInfo;
import com.xunli.filesystem.repository.AppVersionInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shihj on 2017/12/25.
 */
@RestController
@RequestMapping("/quxiangqin")
public class DownLoadAppController {

    @Value("${api.xunli.fileSystem.rootPath}")
    private String rootPath;

    @Value("${api.xunli.pkg}")
    private String defaultPkg;

    @Autowired
    private AppVersionInfoRepository appVersionInfoRepository;

    private static final DateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");

    @RequestMapping(value = "/version/download/{filename}",method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downloadAndroid (@PathVariable  String filename) throws IOException
    {
        if(!filename.endsWith(".apk"))
        {
            filename += ".apk";
        }
        if(!filename.equals(defaultPkg))
        {
            throw new IOException("no apk file");
        }
        AppVersionInfo version = appVersionInfoRepository.findTopByIfUseOrderByCurrentVersionDesc("Y");
        String filePath = String.format("%s%s",rootPath,filename);
        if(!StringUtils.isEmpty(version.getFileName()))
        {
            filePath = String.format("%s%s",rootPath,version.getFileName());
        }
        FileSystemResource file = new FileSystemResource(filePath);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.contentLength())
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(new InputStreamResource(file.getInputStream()));
    }

    @RequestMapping(value = "/version/upload",method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> upload(MultipartFile file)
    {
        Map<String,Object> result = new HashMap<>();
        if(!file.isEmpty())
        {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            try
            {
                if (filename.contains("..") ||
                        (!filename.toLowerCase().endsWith(".apk")))
                {
                    throw new Exception("Cannot store file with relative path outside current directory or not a apk file "+ filename);
                }
                filename = "com.saku.dateone-" + FORMAT.format(new Date())  + filename.substring(filename.lastIndexOf("."));
                Files.copy(file.getInputStream(), Paths.get(rootPath,filename), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            result.put("fileName",filename);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

}
