import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AggregationServerTest {

    @Test
    void testValidPortNumber() {
        int port = AggregationServer.getPort("5678".split(" "));
        assertEquals(5678, port);
    }

    @Test
    void testDefaultPortNumber() {
        int port = AggregationServer.getPort(new String[]{});
        assertEquals(4567, port);
    }

    @Test
    void testExceptionThrownWhenInvalidArgMoreThanOnePort() {
        assertThrows(RuntimeException.class, () -> AggregationServer.getPort(("1234 " +
                "5678").split(" ")));
    }

    @Test
    void testExceptionThrownWhenInvalidArgPortNotInteger() {
        assertThrows(RuntimeException.class,
                () -> AggregationServer.getPort("ASD".split(" ")));
    }


}