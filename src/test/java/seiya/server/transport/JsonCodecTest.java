package seiya.server.transport;

import org.junit.jupiter.api.Test;
import seiya.server.api.ApiModels;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonCodecTest {
    private final JsonCodec jsonCodec = new JsonCodec();

    @Test
    void readsTypedCreateRoomRequest() throws Exception {
        ApiModels.CreateRoomRequest request = jsonCodec.read(
            "{\"character\":\"HYOGA\",\"ruleSet\":\"CLASSIC\"}".getBytes(StandardCharsets.UTF_8),
            ApiModels.CreateRoomRequest.class
        );

        assertEquals("HYOGA", request.getCharacter());
        assertEquals("CLASSIC", request.getRuleSet());
    }

    @Test
    void writesTypedErrorResponse() throws Exception {
        String json = new String(
            jsonCodec.write(new ApiModels.ErrorResponse("Room not found.")),
            StandardCharsets.UTF_8
        );

        assertTrue(json.contains("\"error\":\"Room not found.\""));
    }
}
