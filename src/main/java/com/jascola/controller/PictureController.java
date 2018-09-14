package com.jascola.controller;

import com.jascola.dto.PictureDto;
import com.jascola.entity.PicturesEntity;
import com.jascola.service.Pictureservice;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/pic")
public class PictureController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(PictureController.class);
    private final JedisPool jedisPool;
    private final Pictureservice pictureservice;

    @Autowired
    public PictureController(Pictureservice pictureservice,JedisPool jedisPool) {
        this.pictureservice = pictureservice;
        this.jedisPool = jedisPool;
    }

    @Value("#{prop['resource.realpath']}")
    private String realpath;

    @Value("#{prop['resource.virpath']}")
    private String virpath;

    @Value("#{prop['resource.indexpath']}")
    private String indexpath;

    @Value("#{prop['resource.indexrealpath']}")
    private String indexrealpath;


    @RequestMapping(value = "/upload.html")
    public void upload(HttpServletResponse response, PictureDto formDate, HttpServletRequest request) {
        List<String> messages = new ArrayList<String>();
        Jedis jedis = jedisPool.getResource();
        /*String content = super.tokenAdminCheck(response, request, messages, jedisPool);
        if (content == null) {
            return;
        }*/
        MultipartFile[] files = formDate.getImages();
        PicturesEntity entity = new PicturesEntity();
        MultipartFile file = formDate.getImage();
        if (null != files && files.length > 0 && file != null) {
            File f = new File(realpath + formDate.getPicname());
            if (!f.exists()) {
                f.mkdir();
            }
            for (MultipartFile fl : files) {
                String fileName = fl.getOriginalFilename(); //获得文件名称

                File imagFile = new File(f.getPath(), fileName);

                try {
                    fl.transferTo(imagFile);//用于将文件写到服务器本地

                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                }
            }

            File filex = new File(indexrealpath);
            if (!filex.exists()) {
                filex.mkdir();
            }


            String fileName = file.getOriginalFilename(); //获得文件名称

            File imagFile = new File(filex.getPath(), fileName);
            try {
                file.transferTo(imagFile);//用于将文件写到服务器本地
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
            try {
                entity.setAuthorname(formDate.getAuthorname());
                entity.setIndexpic(indexpath + fileName);
                entity.setIndexrealdir(indexrealpath + fileName);
                entity.setPicname(formDate.getPicname());
                entity.setRealdir(realpath + formDate.getPicname());
                entity.setVirtualdir(virpath + formDate.getPicname());
                entity.setTag(formDate.getTag());
                entity.setId(formDate.getId());
                entity.setCounts(formDate.getCounts());
                if(pictureservice.selectById(formDate.getId()).size()>0){
                    pictureservice.update(entity);
                    jedis.flushDB();
                }
                else {
                    pictureservice.insert(entity);
                    jedis.flushDB();
                }
                messages.add("上传成功");
                super.ResponseSuccess(response, messages);
                return;
            } catch (Exception e) {
                messages.add("上传失败!");
                super.ResponseError(response, messages);
                LOGGER.error(e.getLocalizedMessage(), e);
                return;
            }finally {
                jedis.close();
            }
        }
        messages.add("文件不能为空或重复！");
        super.ResponseError(response, messages);
    }
    @RequestMapping(value = "deletepic.html")
    public void deletePic(String id,HttpServletResponse response,HttpServletRequest request){
        List<String> messages = new ArrayList<String>();
        Jedis jedis = jedisPool.getResource();
        /*String content = super.tokenAdminCheck(response, request, messages, jedisPool);
        if (content == null) {
            return;
        }*/
        try{
            pictureservice.deleteById(id);
            messages.add("删除相册成功");
            jedis.flushDB();
            super.ResponseSuccess(response,messages);
        }catch (Exception e){
            messages.add("删除相册失败");
            super.ResponseError(response,messages);
            LOGGER.error(e.getLocalizedMessage(),e);
        }finally {
            jedis.close();
        }
    }
}