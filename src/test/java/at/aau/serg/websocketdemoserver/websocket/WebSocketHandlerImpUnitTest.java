package at.aau.serg.websocketdemoserver.websocket;

import at.aau.serg.websocketdemoserver.websocket.handler.WebSocketHandlerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WebSocketHandlerImpUnitTest {
    private WebSocketHandler handler;

    @BeforeEach
    void initHandler() {
        handler = new WebSocketHandlerImpl();
    }
    @Test
    void testAfterConnectionEstablishedSessionIsOpen() {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);

        assertDoesNotThrow(() -> handler.afterConnectionEstablished(session));
    }

    @Test
    void testAfterConnectionEstablishedSessionIsOpenForAlreadyOpenConnection() {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(false);


        assertThrows(Exception.class, () -> handler.afterConnectionEstablished(session));
    }

    @Test
    void testHandleMessageResult() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        WebSocketMessage<String> message = mock(WebSocketMessage.class);
        when(message.getPayload()).thenReturn("Test Message");

        handler.handleMessage(session, message);
        verify(session).sendMessage(new TextMessage("echo from handler: Test Message"));
    }

    @Test
    void testHandleTransportErrorShouldThrowException() {
        WebSocketSession session = mock(WebSocketSession.class);
        Throwable exception = mock(Throwable.class);

        assertThrows(Exception.class, () -> handler.handleTransportError(session, exception));
    }

    @Test
    void testAfterConnectionClosedForDefaultClose() {
        WebSocketSession session = mock(WebSocketSession.class);
        CloseStatus closeStatus = CloseStatus.NORMAL;

        assertDoesNotThrow(() -> handler.afterConnectionClosed(session, closeStatus));
    }

    @Test
    void testAfterConnectionClosedForUnexpectedClose() {
        WebSocketSession session = mock(WebSocketSession.class);
        CloseStatus closeStatus = CloseStatus.NOT_ACCEPTABLE;

        assertThrows(Exception.class, () -> handler.afterConnectionClosed(session, closeStatus));
    }

    @Test
    void testSupportsPartialMessages() {
        assertFalse(handler.supportsPartialMessages());
    }
}
