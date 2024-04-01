package at.aau.serg.websocketdemoserver.websocket.handler;

import org.springframework.web.socket.*;

import static org.springframework.web.socket.CloseStatus.NORMAL;

public class WebSocketHandlerImpl implements WebSocketHandler {


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (!session.isOpen())
            throw new Exception("Could not use the provided session:" + session.getId());

    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // TODO handle the messages here
        session.sendMessage(new TextMessage("echo from handler: " + message.getPayload()));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        throw new Exception("Could not handle the transport for session:" + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        if (!closeStatus.equals(NORMAL))
            throw new Exception("Close of session " + session.getId() + " ended in unexpected status:" + closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
