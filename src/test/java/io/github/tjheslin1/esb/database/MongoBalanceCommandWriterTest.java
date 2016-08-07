package io.github.tjheslin1.esb.database;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.tjheslin1.WithMockito;
import io.github.tjheslin1.esb.infrastructure.application.cqrs.deposit.DepositFundsCommand;
import io.github.tjheslin1.esb.infrastructure.application.cqrs.MongoBalanceCommandWriter;
import io.github.tjheslin1.esb.infrastructure.application.cqrs.withdraw.WithdrawFundsCommand;
import io.github.tjheslin1.esb.infrastructure.settings.MongoSettings;
import io.github.tjheslin1.esb.infrastructure.settings.Settings;
import org.assertj.core.api.WithAssertions;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDateTime;

import static io.github.tjheslin1.esb.application.cqrs.command.DepositEventWiring.depositEventWiring;
import static io.github.tjheslin1.esb.application.cqrs.command.WithdrawEventWiring.withdrawalEventWiring;
import static io.github.tjheslin1.esb.infrastructure.application.cqrs.deposit.DepositFundsCommand.depositFundsCommand;
import static io.github.tjheslin1.esb.infrastructure.application.cqrs.withdraw.WithdrawFundsCommand.withdrawFundsCommand;
import static io.github.tjheslin1.esb.infrastructure.mongo.MongoConnection.mongoClient;
import static io.github.tjheslin1.esb.infrastructure.mongo.MongoOperations.collectionNameForEvent;

public class MongoBalanceCommandWriterTest implements WithAssertions, WithMockito {

    private MongoSettings settings = mock(Settings.class);

    private MongoBalanceCommandWriter eventWriter;
    private MongoClient mongoClient;

    private final Clock clock = Clock.systemDefaultZone();

    @Before
    public void before() {
        when(settings.mongoDbPort()).thenReturn(27017);
        when(settings.mongoDbName()).thenReturn("events_store");

        mongoClient = mongoClient(settings);

        eventWriter = new MongoBalanceCommandWriter(mongoClient, settings);
    }

    @After
    public void after() {
        MongoDatabase database = mongoClient.getDatabase(settings.mongoDbName());

        MongoCollection<Document> depositFundsEventsCollection = database
                .getCollection(collectionNameForEvent(DepositFundsCommand.class));

        MongoCollection<Document> withdrawFundsEventsCollection = database
                .getCollection(collectionNameForEvent(WithdrawFundsCommand.class));

        depositFundsEventsCollection.deleteMany(new Document());
        withdrawFundsEventsCollection.deleteMany(new Document());
    }

    @Test
    public void writeDepositFundsEventToDatabaseTest() throws Exception {
        DepositFundsCommand depositFundsCommand = depositFundsCommand(20, 6, LocalDateTime.now(clock));
        eventWriter.write(depositFundsCommand, depositEventWiring());

        assertThat(mongoClient.getDatabase(settings.mongoDbName())
                .getCollection(collectionNameForEvent(depositFundsCommand.getClass()))
                .count())
                .isEqualTo(1);
    }

    @Test
    public void writeWithdrawFundsEventToDatabaseTest() throws Exception {
        WithdrawFundsCommand withdrawFundsCommand = withdrawFundsCommand(20, 6, LocalDateTime.now(clock));
        eventWriter.write(withdrawFundsCommand, withdrawalEventWiring());

        assertThat(mongoClient.getDatabase(settings.mongoDbName())
                .getCollection(collectionNameForEvent(withdrawFundsCommand.getClass()))
                .count())
                .isEqualTo(1);
    }
}