package com.poping.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poping.dto.dataset.DatasetProgressMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * [文件概览]
 * - 目的: 数据集处理进度WebSocket处理器
 */
@Component
public class DatasetProgressWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(DatasetProgressWebSocketHandler.class);

    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String datasetId = extractDatasetId(session.getUri());
        if (datasetId == null) {
            log.warn("WebSocket连接缺少datasetId参数");
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        sessions.computeIfAbsent(datasetId, key -> new CopyOnWriteArraySet<>()).add(session);
        log.debug("WebSocket连接建立, datasetId={}, sessionId={}", datasetId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 当前场景无需处理客户端消息
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().forEach(set -> set.remove(session));
    }

    public void sendProgress(DatasetProgressMessage progressMessage) {
        if (progressMessage == null || progressMessage.getDatasetId() == null) {
            return;
        }
        Set<WebSocketSession> targetSessions = sessions.get(progressMessage.getDatasetId());
        if (targetSessions == null || targetSessions.isEmpty()) {
            return;
        }
        String payload;
        try {
            payload = objectMapper.writeValueAsString(progressMessage);
        } catch (IOException e) {
            log.error("序列化进度消息失败: {}", e.getMessage());
            return;
        }

        for (WebSocketSession session : targetSessions) {
            if (!session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException e) {
                log.warn("发送进度消息失败: {}", e.getMessage());
            }
        }
    }

    private String extractDatasetId(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && "datasetId".equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }
}
