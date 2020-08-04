package com.alinkeji.robot.mirai.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageUtil {

  private static Logger logger = LoggerFactory.getLogger(ImageUtil.class);

  public static void downloadImage(String content) {
    try {
      String path = "/root/web/app/coolq/coolq-pro/data/image/";
      List<String> list = extractMessageByRegular(content);
      list.forEach(cqContent -> {
        if (cqContent.startsWith("CQ:image")) {
          String imagepath = path + cqContent.split(",")[1].replace("file=", "");
          String url = cqContent.split(",")[2].replace("url=", "");
          logger.info("下载图片[{}]到[{}]", url, imagepath);
          download(url, imagepath);
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String convertFileImage(String content) {
    List<String> list = extractMessageByRegular(content);
    for (String cqContent : list) {
      if (cqContent.startsWith("CQ:image") && cqContent.contains("file=") && cqContent
          .contains("url=")) {
        content = content.replaceAll("url=", "file=");
        content = content.replace(cqContent.split(",")[1] + ",", "");
      }
    }
    return content;
  }

  /**
   * 使用正则表达式提取中括号中的内容
   *
   * @param msg
   * @return
   */
  public static List<String> extractMessageByRegular(String msg) {
    List<String> list = new ArrayList<String>();
    Pattern p = Pattern.compile("(\\[[^\\]]*\\])");
    Matcher m = p.matcher(msg);
    while (m.find()) {
      list.add(m.group().substring(1, m.group().length() - 1));
    }
    return list;
  }

  public static void download(String filename, String savePath) {
    // 构造URL
    InputStream is = null;
    OutputStream os = null;
    try {
      java.net.URL url = new java.net.URL(filename);
      // 打开连接
      URLConnection con = url.openConnection();
      // 输入流
      is = con.getInputStream();
      // 1K的数据缓冲
      byte[] bs = new byte[1024];
      // 读取到的数据长度
      int len;
      // 输出的文件流
      os = new FileOutputStream(savePath);
      // 开始读取
      while ((len = is.read(bs)) != -1) {
        os.write(bs, 0, len);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        // 完毕，关闭所有链接
        if (null != is && null != os) {
          is.close();
          os.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
