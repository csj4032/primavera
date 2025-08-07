package com.genius.primavera.batch.config;

import io.debezium.config.Configuration;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebeziumEngineRunner {

    private DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;
    private ExecutorService executor;

    @Value("${debezium.enabled:false}")
    private boolean enabled;

    @Value("${debezium.database.hostname:localhost}")
    private String dbHost;

    @Value("${debezium.database.port:3306}")
    private String dbPort;

    @Value("${debezium.database.user:primavera}")
    private String dbUser;

    @Value("${debezium.database.password:primavera}")
    private String dbPassword;

    @Value("${debezium.database.name:primavera}")
    private String dbName;

    @PostConstruct
    public void start() {
        if (!enabled) {
            log.info("Debezium CDC is disabled");
            return;
        }

        Properties props = new Properties();
        props.setProperty("name", "product-cdc-engine");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        props.setProperty("offset.storage.file.filename", "/tmp/offsets.dat");
        props.setProperty("offset.flush.interval.ms", "60000");
        
        props.setProperty("database.hostname", dbHost);
        props.setProperty("database.port", dbPort);
        props.setProperty("database.user", dbUser);
        props.setProperty("database.password", dbPassword);
        props.setProperty("database.dbname", dbName);
        props.setProperty("database.server.id", "85744");
        props.setProperty("database.server.name", "primavera-server");
        props.setProperty("database.history", "io.debezium.relational.history.FileDatabaseHistory");
        props.setProperty("database.history.file.filename", "/tmp/dbhistory.dat");
        
        props.setProperty("table.include.list", "primavera.PRODUCTS,primavera.SELLERS,primavera.CATEGORIES");
        props.setProperty("include.schema.changes", "false");
        props.setProperty("snapshot.mode", "when_needed");

        Configuration config = Configuration.from(props);

        debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(config.asProperties())
                .notifying(this::handleChangeEvent)
                .build();

        executor = Executors.newSingleThreadExecutor();
        executor.execute(debeziumEngine);
        
        log.info("Debezium CDC engine started for database: {}", dbName);
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> recordChangeEvent) {
        SourceRecord sourceRecord = recordChangeEvent.record();
        
        if (sourceRecord.value() == null) {
            return;
        }

        Struct sourceRecordValue = (Struct) sourceRecord.value();
        
        if (sourceRecordValue != null) {
            String operation = sourceRecordValue.getString("op");
            String table = sourceRecord.topic().split("\\.")[2];
            
            Struct after = sourceRecordValue.getStruct("after");
            Struct before = sourceRecordValue.getStruct("before");
            
            log.debug("CDC Event - Table: {}, Operation: {}", table, operation);
            
            switch (operation) {
                case "c": // CREATE
                    handleInsert(table, after);
                    break;
                case "u": // UPDATE
                    handleUpdate(table, before, after);
                    break;
                case "d": // DELETE
                    handleDelete(table, before);
                    break;
                case "r": // READ (snapshot)
                    handleSnapshot(table, after);
                    break;
            }
        }
    }

    private void handleInsert(String table, Struct data) {
        log.info("Insert detected in table {}: {}", table, structToString(data));
    }

    private void handleUpdate(String table, Struct before, Struct after) {
        log.info("Update detected in table {}: {} -> {}", table, structToString(before), structToString(after));
    }

    private void handleDelete(String table, Struct data) {
        log.info("Delete detected in table {}: {}", table, structToString(data));
    }

    private void handleSnapshot(String table, Struct data) {
        log.debug("Snapshot record from table {}: {}", table, structToString(data));
    }

    private String structToString(Struct struct) {
        if (struct == null) return "null";
        
        StringBuilder sb = new StringBuilder("{");
        for (Field field : struct.schema().fields()) {
            sb.append(field.name()).append("=").append(struct.get(field)).append(", ");
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}");
        return sb.toString();
    }

    @PreDestroy
    public void stop() {
        if (debeziumEngine != null) {
            try {
                debeziumEngine.close();
                log.info("Debezium engine stopped");
            } catch (IOException e) {
                log.error("Error stopping Debezium engine", e);
            }
        }
        
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
