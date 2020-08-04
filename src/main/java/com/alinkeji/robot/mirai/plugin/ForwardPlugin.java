package com.alinkeji.robot.mirai.plugin;

import com.alibaba.fastjson.JSONObject;
import com.alinkeji.robot.mirai.enums.CommandEnum;
import com.alinkeji.robot.mirai.util.ForyouStack;
import com.alinkeji.robot.mirai.util.HttpUtil;
import com.alinkeji.robot.mirai.util.ImageUtil;
import com.alinkeji.robot.mirai.util.StringUtil;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.lz1998.cq.CQGlobal;
import net.lz1998.cq.event.message.CQGroupMessageEvent;
import net.lz1998.cq.event.message.CQPrivateMessageEvent;
import net.lz1998.cq.robot.CQPlugin;
import net.lz1998.cq.robot.CoolQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 转发插件
 *
 * @author linrol
 */
@SuppressWarnings("unchecked")
@Component
public class ForwardPlugin extends CQPlugin {

  private static Logger logger = LoggerFactory.getLogger(ForwardPlugin.class);

  public static Map<String, String> pidMap = new HashMap<String, String>();

  public static Map<String, List<String>> monitorUserMap = new HashMap<String, List<String>>();

  private static ForyouStack<String> msgStack = new ForyouStack<String>(20);

  static {
    pidMap.put("910092655", "mm_109302870_1080150328_109752250051");
    pidMap.put("198896490", "mm_109302870_1090250211_109781850271");
    pidMap.put("963559879", "mm_109302870_1090250211_109781850271");
    monitorUserMap.put("1706860030", Arrays.asList("3317628455", "2267793115", "1096471489"));
    monitorUserMap.put("779721310", Arrays.asList("1012230561", "1256647017", "1071893649"));
  }

  public int onPrivateMessage(CoolQ cq, CQPrivateMessageEvent event) {
    logger.info("QQ:{}收到好友:{}消息", cq.getSelfId(), event.getUserId());
    String msg = filterMsg(event.getMessage());
    // ImageUtil.downloadImage(event.getMessage());
    List<String> sourceContentList = getSourceContent(msg);
    if (sourceContentList.size() < 1 || cq.getSelfId() != 779721310) {
      return MESSAGE_IGNORE;
    }
    String newMsg = convertMsg(sourceContentList,String.join("\n",sourceContentList),pidMap.get(String.valueOf(910092655l)));
    if (StringUtils.isEmpty(newMsg)) {
      return MESSAGE_IGNORE;
    }
    if(!newMsg.contains("http")){
      newMsg = "￥" + newMsg + "￥";
    }
    cq.sendPrivateMsg(event.getUserId(), newMsg, false);
    return MESSAGE_IGNORE; // 继续执行下一个插件
  }

  @Override
  public int onGroupMessage(CoolQ cq, CQGroupMessageEvent event) {
    logger.info("QQ:{}收到群:{}(发送者角色:{})消息", cq.getSelfId(), event.getGroupId(),event.getSender().getRole());
    Map<Long,List<Long>> monitorForwardGroupMap = ((Map<Long, Map<Long,List<Long>>>) CommandPlugin.config
        .get(CommandEnum.MONITOR_GROUP_ID_LIST.getCommand())).get(cq.getSelfId());
    long group_id = event.getGroupId();
    String msg = filterMsg(event.getMessage());
    //if(!monitorGrouplist.contains(group_id) || !monitorUserMap.get(String.valueOf(cq.getSelfId())).contains(String.valueOf(event.getUserId()))) {
    if (monitorForwardGroupMap == null || !monitorForwardGroupMap.containsKey(group_id)) {
      return MESSAGE_IGNORE;
    }
    if (msgStack.containLike(StringUtil.getChineseString(msg), 0.8f)) {
      logger.info("消息[{}]大于相似因子0.8，放弃本次消息转发", StringUtil.getChineseString(msg));
      return MESSAGE_IGNORE;
    }
    if(event.getSender().getRole().equals("member")){
      logger.info("发送者角色:{},不为管理员，放弃本次消息转发", event.getSender().getRole());
      return MESSAGE_IGNORE;
    }
    msgStack.push(StringUtil.getChineseString(msg));
    // ImageUtil.downloadImage(event.getMessage());
    List<Long> forwardGrouplist = monitorForwardGroupMap.get(group_id);
    List<String> sourceContentList = getSourceContent(msg);
    forwardGrouplist.forEach(groupId -> CQGlobal.robots.get(779721310l).sendGroupMsg(groupId,
        convertMsg(sourceContentList, ImageUtil.convertFileImage(msg), pidMap.get(String.valueOf(groupId))), false));
    return MESSAGE_IGNORE; // 继续执行下一个插件
    // return MESSAGE_BLOCK; // 不执行下一个插件
  }

  private static List<String> getSourceContent(String msg) {
    List<String> sourceList = new ArrayList<String>();
    Pattern p1 = Pattern.compile("([\\p{Sc}])\\w{8,12}([\\p{Sc}])");
    Matcher m1 = p1.matcher(msg);
    if (m1.find()) {
      String source = m1.group();
      source = source.substring(1, source.length() - 1);
      sourceList.add(source);
      return sourceList;
    }
    Pattern p2 = Pattern.compile("(\\()([0-9a-zA-Z\\.\\/\\=])*(\\))");
    Matcher m2 = p2.matcher(msg);
    if (m2.find()) {
      String source = m2.group(0);
      source = source.substring(1, source.length() - 1);
      sourceList.add(source);
      return sourceList;
    }
    Pattern p3 = Pattern.compile(
        "http[s]?://(?:(?!http[s]?://)[a-zA-Z]|[0-9]|[$\\-_@.&+/]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+");
    Matcher m3 = p3.matcher(msg);
    while (m3.find()) {
      sourceList.add(m3.group());
    }
    if (sourceList.size() > 0) {
      return sourceList;
    }
    logger.error("待发送内容:{},未匹配到淘口令或URL", msg);
    return sourceList;
  }

  private static String convertMsg(List<String> sourceContentList, String msg, String pid) {
    for (String sourceContent : sourceContentList) {
      if(sourceContent.contains("http") && !sourceContent.contains(".jd.com")) {
        continue;
      }
      String newContent = sourceContent.contains(".jd.com") ? getJdShortUrl(sourceContent):getChangeTklBy21ds("￥" + sourceContent + "￥", pid);
      logger.info("source:" + sourceContent + "-----new:" + newContent.replaceAll("￥", ""));
      msg = msg.replace(sourceContent, newContent.replaceAll("￥", ""));
    }
    return msg;
  }

  private static String getChangeTklBy21ds(String sourceTkl, String pid) {
    String url = "http://api.web.21ds.cn/taoke/doItemHighCommissionPromotionLinkByTpwd?apkey=%s&tpwdcode=%s&pid=%s&tbname=%s&tpwd=1&extsearch=1";
    String apKey = "7918202b-ef4a-f251-291b-eb880302814c";
    String tbname = "tb6746204";
    JSONObject jsonResult;
    try {
      jsonResult = HttpUtil.sendGet(String.format(url, apKey, sourceTkl, pid, tbname));
      logger.info("喵有券接口转换结果：" + jsonResult.toJSONString());
      if (jsonResult.getInteger("code") == 200) {
        return jsonResult.getJSONObject("data").getString("tpwd");
      }
      //logger.error("获取喵有券高佣转淘口令api接口错误，请立即排查处理，否则影响引导收入，失败原因:[{}]",DsErrorEnum.getByCode(jsonResult.getString("code")));
      return analyzeAndCreateNewTkl(sourceTkl);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return sourceTkl;
  }

  public static String analyzeAndCreateNewTkl(String sourceTkl) {
    String analyzeUrl = "http://api.web.21ds.cn/taoke/jiexitkl?apkey=%s&kouling=%s";
    String createUrl = "http://api.web.21ds.cn/taoke/createTaoPwd?apkey=%s&url=%s&title=%s&pic=%s";
    String apKey = "7918202b-ef4a-f251-291b-eb880302814c";
    try {
      JSONObject analyzeJsonResult = HttpUtil.sendGet(String.format(analyzeUrl, apKey, sourceTkl));
      if (analyzeJsonResult.getInteger("code") != 200) {
        return sourceTkl;
      }
      String url = URLEncoder
          .encode(analyzeJsonResult.getJSONObject("data").getString("url"), "utf-8");
      String title = URLEncoder.encode("免单群910092655", "utf-8");
      String pic = URLEncoder
          .encode(analyzeJsonResult.getJSONObject("data").getString("pic"), "utf-8");
      JSONObject createJsonResult = HttpUtil
          .sendGet(String.format(createUrl, apKey, url, title, pic));
      if (createJsonResult.getInteger("code") != 200) {
        return sourceTkl;
      }
      return createJsonResult.getString("data");
    } catch (Exception e) {
      e.printStackTrace();
      return sourceTkl;
    }
  }

  private static String getJdShortUrl(String sourceShortUrl) {
    String url = "http://api.web.21ds.cn/jingdong/doItemCpsUrl?apkey=%s&materialId=%s&key_id=%s";
    String apKey = "7918202b-ef4a-f251-291b-eb880302814c";
    String jdKeyId = "759e5b5d-4b6d-8e63-b3ac-65353c2ac7ea";
    try {
      JSONObject result = HttpUtil
          .sendGet(String.format(url, apKey, URLEncoder.encode(sourceShortUrl, "utf-8"), jdKeyId));
      if (result.getInteger("code") != 200) {
        return sourceShortUrl;
      }
      return result.getJSONObject("data").getString("shortURL");
    } catch (Exception e) {
      e.printStackTrace();
      return sourceShortUrl;
    }
  }

  private String filterMsg(String msg) {
    msg = msg.replaceAll("元", "").replaceAll("生活费147223", "生活费3925276");
    for (Map.Entry<String, String> entry : CommandPlugin.filterMap.entrySet()) {
      msg = msg.replaceAll(entry.getKey(), entry.getValue());
    }
    return msg;
  }
}
