package io.github.tjheslin1.esb.database;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.tjheslin1.esb.infrastructure.mongo.MongoConnection;
import io.github.tjheslin1.esb.infrastructure.application.cqrs.command.MongoBalanceEventWriter;
import io.github.tjheslin1.esb.infrastructure.application.events.DepositFundsCommand;
import io.github.tjheslin1.esb.infrastructure.application.events.WithdrawFundsCommand;
import io.github.tjheslin1.esb.infrastructure.settings.MongoSettings;
import io.github.tjheslin1.esb.infrastructure.settings.TestSettings;
import org.assertj.core.api.WithAssertions;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDateTime;

import static io.github.tjheslin1.esb.infrastructure.mongo.MongoOperations.collectionNameForEvent;
import static io.github.tjheslin1.esb.application.eventwiring.DepositEventWiring.depositEventWiring;
import static io.github.tjheslin1.esb.infrastructure.application.events.DepositFundsCommand.depositFundsCommand;
import static io.github.tjheslin1.esb.application.eventwiring.WithdrawEventWiring.withdrawalEventWiring;
import static io.github.tjheslin1.esb.infrastructure.application.events.WithdrawFundsCommand.withdrawFundsCommand;

public class MongoBalanceEventWriterTest implements WithAssertions {

    private MongoSettings mongoSettings = new TestSettings();
    private MongoConnection mongoConnection = new MongoConnection(mongoSettings);

    private MongoBalanceEventWriter eventWriter;
    private MongoClient mongoClient;

    private final Clock clock = Clock.systemDefaultZone();

    @Before
    public void before() {
        mongoClient = mongoConnection.connection();
        eventWriter = new MongoBalanceEventWriter(mongoClient, mongoSettings);
    }

    @After
    public void after() {
        MongoDatabase database = mongoClient.getDatabase(mongoSettings.mongoDbName());

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

        assertThat(mongoClient.getDatabase(mongoSettings.mongoDbName())
                .getCollection(collectionNameForEvent(depositFundsCommand.getClass()))
                .count())
                .isEqualTo(1);
    }

    @Test
    public void writeWithdrawFundsEventToDatabaseTest() throws Exception {
        WithdrawFundsCommand withdrawFundsCommand = withdrawFundsCommand(20, 6, LocalDateTime.now(clock));
        eventWriter.write(withdrawFundsCommand, withdrawalEventWiring());

        assertThat(mongoClient.getDatabase(mongoSettings.mongoDbName())
                .getCollection(collectionNameForEvent(withdrawFundsCommand.getClass()))
                .count())
                .isEqualTo(1);
    }
}