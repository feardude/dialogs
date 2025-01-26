package ru.smax.social.network.dialog;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Repository
class DialogRepository {
    private static final RowMapper<Message> ROW_MAPPER_MESSAGE =
            (rs, _) -> Message.builder()
                              .id(UUID.fromString(rs.getString("id")))
                              .author(rs.getInt("author"))
                              .text(rs.getString("text"))
                              .sentAt(rs.getTimestamp("sent_at").toLocalDateTime())
                              .build();

    private final JdbcTemplate jdbcTemplate;

    public void saveMessage(Message newMessage) {
        var sql = """
                        insert into messages (id, dialog_id, author, text, sent_at)
                        values (?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                newMessage.id(),
                newMessage.dialogId(),
                newMessage.author(),
                newMessage.text(),
                newMessage.sentAt()
        );
    }

    public List<Message> findDialog(String dialogId) {
        var sql = """
                    select id, author, text, sent_at
                    from messages
                    where dialog_id = ?
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER_MESSAGE, dialogId);
    }

    public void createDialog(String dialogId) {
        jdbcTemplate.update("insert into dialogs values (?)", dialogId);
    }

    public void createDialogs(List<Object[]> ids) {
        jdbcTemplate.batchUpdate("insert into dialogs values (?)", ids);
    }
}
