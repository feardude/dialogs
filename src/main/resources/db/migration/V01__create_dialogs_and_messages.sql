-- шардированная таблица диалогов
create table dialogs (
    id text not null
);
alter table dialogs add constraint dialogs_pk primary key (id);
select create_distributed_table('dialogs', 'id');

-- связанная таблица сообщений – нужно распределить данные по тем же шардам, что и таблица dialogs
create table messages (
    id        uuid not null default gen_random_uuid(),
    dialog_id text not null references dialogs (id),
    author    int  not null,
    text      text not null,
    sent_at   timestamp
);

-- составной PK – чтобы можно было шардировать по dialog_id
-- при этом именно id является уникальным идентификатором сообщения
alter table messages add constraint messages_pk primary key (dialog_id, id);
select create_distributed_table('messages', 'dialog_id', colocate_with => 'dialogs');
