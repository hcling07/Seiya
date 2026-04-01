package seiya.game;

import seiya.actions.Action;
import seiya.actions.Attack;
import seiya.characters.Character;
import seiya.util.NumberFormatter;

import java.util.ArrayList;
import java.util.List;

public final class TurnResolver {
    private TurnResolver() {
    }

    public static List<String> resolve(Player playerOne, Action actionOne, Player playerTwo, Action actionTwo) {
        List<String> logs = new ArrayList<>();
        boolean oneBlocked = actionOne.attackValue() <= actionTwo.defenseValue();
        boolean twoBlocked = actionTwo.attackValue() <= actionOne.defenseValue();

        logs.add(describeAction(playerOne, actionOne, playerTwo, actionTwo, oneBlocked));
        logs.add(describeAction(playerTwo, actionTwo, playerOne, actionOne, twoBlocked));
        logs.add(playerOne.name() + ": " + executeAction(playerOne, actionOne, playerTwo, actionTwo, oneBlocked));
        logs.add(playerTwo.name() + ": " + executeAction(playerTwo, actionTwo, playerOne, actionOne, twoBlocked));
        return logs;
    }

    private static String describeAction(Player actor, Action action, Player target, Action opposingAction, boolean blockedByDefense) {
        String clashOutcome = blockedByDefense
            ? "blocked by opposing defense"
            : "breaks opposing defense";
        return actor.name() + ": " + describeActionValues(action, actor.character(), target.character(), opposingAction, blockedByDefense)
            + ", opposingDefense=" + NumberFormatter.fmt(opposingAction.defenseValue())
            + ", clashResult=" + clashOutcome;
    }

    private static String describeActionValues(Action action, Character actor, Character target, Action opposingAction, boolean blockedByDefense) {
        double projectedDamage = 0.0;
        if (action.attackValue() > 0.0 && !blockedByDefense) {
            projectedDamage = target.previewDamageTaken(action.attackValue() - opposingAction.defenseValue());
        }
        return action.name()
            + " [attack=" + NumberFormatter.fmt(action.attackValue())
            + ", defense=" + NumberFormatter.fmt(action.defenseValue())
            + ", spiritCost=" + NumberFormatter.fmt(action.spiritCost())
            + ", actorSpirit=" + NumberFormatter.fmt(actor.spirit())
            + ", targetArmor=" + target.armorWorn()
            + ", targetDefend=" + target.defendPercent() + "%"
            + ", projectedDamage=" + NumberFormatter.fmt(projectedDamage) + "]";
    }

    private static String executeAction(Player actor, Action action, Player target, Action opposingAction, boolean blockedByDefense) {
        String result;
        if (action instanceof Attack) {
            result = ((Attack) action).executeForClash(
                actor.character(),
                target.character(),
                opposingAction.defenseValue(),
                blockedByDefense
            );
        } else {
            result = actor.executeAction(action, target);
        }
        actor.recordTurn();
        return result;
    }
}
