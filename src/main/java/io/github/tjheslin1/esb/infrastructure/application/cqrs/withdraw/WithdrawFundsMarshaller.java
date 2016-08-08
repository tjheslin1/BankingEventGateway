package io.github.tjheslin1.esb.infrastructure.application.cqrs.withdraw;

import io.github.tjheslin1.esb.application.cqrs.command.EventJsonMarshaller;
import io.github.tjheslin1.esb.domain.events.BalanceCommand;
import org.bson.Document;

public class WithdrawFundsMarshaller implements EventJsonMarshaller {

    @Override
    public Document marshallBalanceEvent(BalanceCommand balanceCommand) {
        WithdrawFundsCommand withdrawFundsCommand = (WithdrawFundsCommand) balanceCommand;

        Document withdrawDoc = new Document("timeOfEvent", withdrawFundsCommand.timeOfEvent());
        withdrawDoc.append("accountId", withdrawFundsCommand.accountId());
        withdrawDoc.append("amount", withdrawFundsCommand.amount());
        return withdrawDoc;
    }
}