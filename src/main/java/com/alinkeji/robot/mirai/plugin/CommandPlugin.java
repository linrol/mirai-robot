package com.alinkeji.robot.mirai.plugin;

import com.alinkeji.robot.mirai.enums.CommandEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.lz1998.cq.event.message.CQPrivateMessageEvent;
import net.lz1998.cq.robot.CQPlugin;
import net.lz1998.cq.robot.CoolQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 配置插件
 *
 * @author linrol
 */
@Component
public class CommandPlugin extends CQPlugin {

  public static Map<String, Object> config = new HashMap<String, Object>();

  public static Map<String, String> filterMap = new HashMap<String, String>();

  static {
    List<Long> lcontrollerList = new ArrayList<Long>();
    lcontrollerList.add(1071893649l);
    config.put(CommandEnum.CONTROLLER_QQ_LIST.getCommand(), lcontrollerList);
    Map<Long, Map<Long,List<Long>>> monitorGroupMap = new HashMap<>();
    Map<Long,List<Long>> monitorForwardGroupMap = new HashMap<>();
    monitorForwardGroupMap.put(914494716l,Arrays.asList(910092655l,198896490l));
    monitorForwardGroupMap.put(425239590l,Arrays.asList(963550879l));
    monitorGroupMap.put(1706860030l, monitorForwardGroupMap);
    config.put(CommandEnum.MONITOR_GROUP_ID_LIST.getCommand(), monitorGroupMap);

    //List<Long> forwardGroupList = new ArrayList<Long>();
    //forwardGroupList.add(910092655l);
    //forwardGroupList.add(198896490l);
    //config.put(CommandEnum.FORWARD_GROUP_ID_LIST.getCommand(), forwardGroupList);

    filterMap.put("自助优惠券商城\r\n查券找好物点击~~~\r\n", "");
    filterMap.put("http://kpeyfty.asia", "");
  }

  private Logger logger = LoggerFactory.getLogger(CommandPlugin.class);

  @Override
  @SuppressWarnings("unchecked")
  public int onPrivateMessage(CoolQ cq, CQPrivateMessageEvent event) {
    //long userId = event.getSender().getUserId();
    String msg = event.getMessage();
    List<Long> controllerQQlist = (List<Long>) config
        .get(CommandEnum.CONTROLLER_QQ_LIST.getCommand());
    if (!msg.startsWith("指令-")) {
      return MESSAGE_IGNORE;
    }
    String[] msgs = msg.split("-");
    CommandEnum commandEnum = CommandEnum.getCommandEnum(msgs[1]);
    if (commandEnum == null || msgs.length < 3) {
      logger.error("未知指令或指令格式不对:{}", msg);
      // cq.sendPrivateMsg(userId, "未知指令或指令格式不对", false);
      return MESSAGE_BLOCK;
    }
    if (commandEnum == CommandEnum.CONTROLLER_QQ_ADD && !controllerQQlist
        .contains(Long.parseLong(msgs[2]))) {
      controllerQQlist.add(Long.parseLong(msgs[2]));
      config.put(CommandEnum.CONTROLLER_QQ_LIST.getCommand(), controllerQQlist);
      // cq.sendPrivateMsg(userId, "主人[" + msgs[2] + "]已添加,即刻开始enjoy吧!", false);
      logger.info("主人QQ[{}]已添加,即刻开始enjoy吧!", msgs[2]);
      return MESSAGE_BLOCK;
    } else if (commandEnum == CommandEnum.MONITOR_GROUP_ID_ADD) {
      Map<Long, List<Long>> monitorGroupMap = (Map<Long, List<Long>>) config
          .get(CommandEnum.MONITOR_GROUP_ID_LIST.getCommand());
      List<Long> monitorGrouplist = monitorGroupMap.get(cq.getSelfId());
      if (monitorGrouplist.contains(Long.parseLong(msgs[2]))) {
        logger.error("监听QQ群号[{}]已在列表中，请不要重复添加", msgs[2]);
        return MESSAGE_BLOCK;
      }
      monitorGrouplist.add(Long.parseLong(msgs[2]));
      monitorGroupMap.put(cq.getSelfId(), monitorGrouplist);
      config.put(CommandEnum.MONITOR_GROUP_ID_LIST.getCommand(), monitorGroupMap);
      logger.info("监听QQ群号[{}]已添加", msgs[2]);
      // cq.sendPrivateMsg(userId, "监听G[" + msgs[2] + "]已添加", false);
      return MESSAGE_BLOCK;
    } else if (commandEnum == CommandEnum.MONITOR_GROUP_ID_REMOVE) {
      Map<Long, List<Long>> monitorGroupMap = (Map<Long, List<Long>>) config
          .get(CommandEnum.MONITOR_GROUP_ID_LIST.getCommand());
      List<Long> monitorGrouplist = monitorGroupMap.get(cq.getSelfId());
      if (!monitorGrouplist.contains(Long.parseLong(msgs[2]))) {
        logger.error("监听QQ群号[{}]已不在列表中，无需移除", msgs[2]);
        return MESSAGE_BLOCK;
      }
      monitorGrouplist.remove(Long.parseLong(msgs[2]));
      monitorGroupMap.put(cq.getSelfId(), monitorGrouplist);
      config.put(CommandEnum.MONITOR_GROUP_ID_LIST.getCommand(), monitorGroupMap);
      logger.info("监听QQ群号[{}]已移除", msgs[2]);
      // cq.sendPrivateMsg(userId, "监听G[" + msgs[2] + "]已添加", false);
      return MESSAGE_BLOCK;
    }else if (commandEnum == CommandEnum.FORWARD_MSG_FILTER) {
      filterMap.put(msgs[2], msgs[3]);
      logger.info("转发消息中[{}]过滤[{}]成功", msgs[2], msgs[3]);
      // cq.sendPrivateMsg(userId, "转发G[" + msgs[2] + "]已添加", false);
      return MESSAGE_BLOCK;
    } else {
      logger.error("指令正在{}开发中...", commandEnum.getDesc());
      // cq.sendPrivateMsg(userId, "指令正在{" + commandEnum.getDesc() + "}开发中...", false);
    }
    return MESSAGE_BLOCK; // 继续执行下一个插件
    // return MESSAGE_BLOCK; // 不执行下一个插件
  }
}
