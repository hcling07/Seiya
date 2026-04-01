package seiya.characters;

import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;
import seiya.game.RuleSet;

import java.util.Arrays;
import java.util.List;

public class Shiryu extends Character {
    public Shiryu() {
        this(RuleSet.DEFAULT);
    }

    public Shiryu(RuleSet ruleSet) {
        super(
            ruleSet,
            "Shiryu",
            36,
            ruleSet == RuleSet.CLASSIC ? 2 : 4,
            0,
            attacks(ruleSet),
            consumables(ruleSet)
        );
    }

    private static List<Attack> attacks(RuleSet ruleSet) {
        if (ruleSet == RuleSet.CLASSIC) {
            return Arrays.asList(
                new Attack("Rozan Rising Dragon", 1, 1, 1),
                new Attack("Rozan Dragon Flight", 2, 2, 6)
            );
        }

        return Arrays.asList(
            new Attack("Rozan Rising Dragon", 3, 10),
            new Attack("Rozan Dragon Flight", 5, 14)
        );
    }

    private static List<ConsumableAttack> consumables(RuleSet ruleSet) {
        if (ruleSet == RuleSet.CLASSIC) {
            return Arrays.asList();
        }

        return Arrays.asList(
            new ConsumableAttack("Dragon Shield Shard", 0, 4),
            new ConsumableAttack("Mountain Crusher", 2, 9)
        );
    }
}
