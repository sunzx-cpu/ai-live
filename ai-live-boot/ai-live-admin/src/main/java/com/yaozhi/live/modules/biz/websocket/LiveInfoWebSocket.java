package com.yaozhi.live.modules.biz.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yaozhi.live.modules.biz.entity.LiveInteractKeywordEntity;
import com.yaozhi.live.modules.biz.service.LiveInteractKeywordService;
import com.yaozhi.live.modules.biz.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 直播端推送消息
 */
@Component
@ServerEndpoint("/websocket/liveInfo/{userId}")
@Slf4j
public class LiveInfoWebSocket {
    private static MerchantService merchantService;
    private static LiveInteractKeywordService liveInteractKeywordService;

    @Autowired
    public void setMerchantService(MerchantService merchantService) {
        LiveInfoWebSocket.merchantService = merchantService;
    }
    @Autowired
    public void setLiveInteractKeywordService(LiveInteractKeywordService liveInteractKeywordService) {
        LiveInfoWebSocket.liveInteractKeywordService = liveInteractKeywordService;
    }

    // 存储每个商户ID对应的WebSocket会话集合（线程安全）
    private static final ConcurrentHashMap<Long, CopyOnWriteArraySet<LiveInfoWebSocket>> merchantSessionMap = new ConcurrentHashMap<>();
    // 当前连接的会话
    private Session session;
    // 当前连接的商户ID
    private Long merchantId;

    /**
     * 连接建立成功时调用的方法
     * @param session 会话
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        this.session = session;
        this.merchantId = merchantService.getMerchantId(userId);

        // 将当前连接会话加入到对应商户ID的集合中
        merchantSessionMap.putIfAbsent(merchantId, new CopyOnWriteArraySet<>());
        merchantSessionMap.get(merchantId).add(this);

        log.info("商户[{}]连接成功，当前在线连接数: {}", merchantId, merchantSessionMap.get(merchantId).size());
    }

    /**
     * 连接关闭时调用的方法
     */
    @OnClose
    public void onClose() {
        // 从Map中移除当前会话
        merchantSessionMap.get(merchantId).remove(this);
        if (merchantSessionMap.get(merchantId).isEmpty()) {
            merchantSessionMap.remove(merchantId);
        }
        log.info("商户[{}]连接断开", merchantId);
    }

    /**
     * 收到客户端消息时调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            // 解析客户端发送的消息
            JSONObject jsonObject = JSONUtil.parseObj(message);
            String type = jsonObject.getStr("type");

            // 根据消息类型分发处理
            switch (type) {
                case "audio_stream_start":
                    handleAudioStreamStart(jsonObject);
                    break;
                case "audio_stream_data":
                    handleAudioStreamData(jsonObject);
                    break;
                case "audio_stream_end":
                    handleAudioStreamEnd(jsonObject);
                    break;
                case "interactKeyword":
                    handleInteractKeyword(jsonObject, session);
                    break;
                default:
                    log.warn("收到未知类型消息: type={}", type);
                    break;
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    /**
     * 处理音频流开始消息（Electron端发送）
     * 转发给同一商户的所有App端连接
     */
    private void handleAudioStreamStart(JSONObject message) {
        String streamId = message.getStr("streamId");
        String text = message.getStr("text");

        log.info("商户[{}]开始音频推流: streamId={}, text={}", merchantId, streamId, text);

        // 移除merchantId字段，避免泄露给App端
        message.remove("merchantId");

        // 转发给App端
        sendMessageToMerchant(merchantId, JSONUtil.toJsonStr(message));
    }

    /**
     * 处理音频流数据消息（Electron端发送）
     * 直接转发，不做处理（高性能）
     * 使用同步发送避免并发问题
     */
    private void handleAudioStreamData(JSONObject message) {
        // 音频数据块使用同步发送（避免高频率消息的并发冲突）
        sendMessageToMerchantSync(merchantId, JSONUtil.toJsonStr(message));
    }

    /**
     * 处理音频流结束消息（Electron端发送）
     */
    private void handleAudioStreamEnd(JSONObject message) {
        String streamId = message.getStr("streamId");
        Double duration = message.getDouble("duration");

        log.info("商户[{}]音频推流结束: streamId={}, duration={}s", merchantId, streamId, duration);

        // 转发给App端
        sendMessageToMerchant(merchantId, JSONUtil.toJsonStr(message));
    }

    /**
     * 处理交互关键字消息（心跳）
     */
    private void handleInteractKeyword(JSONObject message, Session session) {
        try {
            Long userId = message.getLong("userId");
            log.info("收到来自商户[{}]的心跳", userId);
            // 发送回客户端
            session.getBasicRemote().sendText(getBusinessDataForMerchant(userId));
        } catch (Exception e) {
            log.error("处理心跳消息失败", e);
        }
    }

    /**
     * 根据商户ID获取需要推送的业务数据
     * 此处需要您根据实际业务实现
     */
    private String getBusinessDataForMerchant(Long userId) {
        List<LiveInteractKeywordEntity> list = liveInteractKeywordService.list(Wrappers.<LiveInteractKeywordEntity>lambdaQuery()
                .eq(LiveInteractKeywordEntity::getMerchantId, merchantService.getMerchantId(userId)));
        // 构建响应消息，将业务数据返回给客户端
        JSONObject response = new JSONObject();
        response.put("type", "interactKeyword_ack");
        response.put("data", JSONUtil.toJsonStr(list));
        response.put("timestamp", System.currentTimeMillis());
        return JSONUtil.toJsonStr(response);
    }

    /**
     * 向指定商户ID发送消息（异步发送，用于低频率消息）
     * @param merchantId 商户ID
     * @param message 消息内容
     */
    public static void sendMessageToMerchant(Long merchantId, String message) {
        CopyOnWriteArraySet<LiveInfoWebSocket> sessions = merchantSessionMap.get(merchantId);
        if (sessions != null && !sessions.isEmpty()) {
            for (LiveInfoWebSocket webSocket : sessions) {
                try {
                    // 异步发送消息
                    webSocket.session.getAsyncRemote().sendText(message);
                } catch (Exception e) {
                    log.error("发送消息失败", e);
                }
            }
        }
    }

    /**
     * 向指定商户ID发送消息（同步发送，用于高频率消息如音频数据块）
     * 同步发送避免并发冲突，适用于音频流等高频率场景
     * @param merchantId 商户ID
     * @param message 消息内容
     */
    public static void sendMessageToMerchantSync(Long merchantId, String message) {
        CopyOnWriteArraySet<LiveInfoWebSocket> sessions = merchantSessionMap.get(merchantId);
        if (sessions != null && !sessions.isEmpty()) {
            for (LiveInfoWebSocket webSocket : sessions) {
                try {
                    // 同步发送消息（阻塞，但避免并发问题）
                    webSocket.session.getBasicRemote().sendText(message);
                } catch (Exception e) {
                    log.error("发送消息失败", e);
                }
            }
        }
    }
}
