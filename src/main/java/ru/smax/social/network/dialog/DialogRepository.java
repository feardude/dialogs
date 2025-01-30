package ru.smax.social.network.dialog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
class DialogRepository {
    private final JedisPool jedisPool;
    private final String scriptSha;
    private final ObjectMapper objectMapper;

    public DialogRepository(JedisPool jedisPool) {
        this.jedisPool = jedisPool;

        String script = """
                    local function get_messages(dialog_id)
                        return redis.call("LRANGE", "messages:" .. dialog_id, 0, -1)
                    end

                    local function send_message(dialog_id, author, text, sent_at)
                        local message = cjson.encode({author = author, text = text, sent_at = sent_at})
                        redis.call("RPUSH", "messages:" .. dialog_id, message)
                        return "Message sent"
                    end

                    if ARGV[1] == "get_messages" then
                        return get_messages(ARGV[2])
                    elseif ARGV[1] == "send_message" then
                        return send_message(ARGV[2], ARGV[3], ARGV[4], ARGV[5])
                    end
                """;
        this.scriptSha = jedisPool.getResource().scriptLoad(script);
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public void sendMessage(Message newMessage) {
        try (Jedis resource = jedisPool.getResource()) {
            resource.evalsha(scriptSha, 0,
                    "send_message",
                    newMessage.dialogId(),
                    String.valueOf(newMessage.author()),
                    newMessage.text(),
                    String.valueOf(newMessage.sentAt())
            );
        }
    }

    @SuppressWarnings("unchecked")
    public List<Message> findDialog(String dialogId) {
        Object result;
        try (Jedis resource = jedisPool.getResource()) {
            result = resource.evalsha(scriptSha, 0, "get_messages", dialogId);
        }

        if (result instanceof List) {
            return ((List<String>) result).stream()
                                          .map(row -> {
                                              try {
                                                  return objectMapper.readValue(row, Message.class);
                                              } catch (JsonProcessingException e) {
                                                  log.info("Could not parse row: %s".formatted(row), e);
                                                  return null;
                                              }
                                          })
                                          .filter(Objects::nonNull)
                                          .toList();
        }

        return List.of();
    }
}
