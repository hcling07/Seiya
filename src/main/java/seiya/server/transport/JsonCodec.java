package seiya.server.transport;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class JsonCodec {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] write(Object value) throws IOException {
        return objectMapper.writeValueAsBytes(value);
    }

    public <T> T read(byte[] value, Class<T> type) throws IOException {
        return objectMapper.readValue(value, type);
    }
}
