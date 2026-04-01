package seiya.characters;

import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;
import seiya.game.RuleSet;

import java.util.Arrays;
import java.util.List;

public class Hyoga extends Character {
    public Hyoga() {
        this(RuleSet.DEFAULT);
    }

    public Hyoga(RuleSet ruleSet) {
        super(
            ruleSet,
            "Hyoga",
            34,
            ruleSet == RuleSet.CLASSIC ? 2 : 3,
            0,
            attacks(ruleSet),
            consumables(ruleSet)
        );
    }

    private static List<Attack> attacks(RuleSet ruleSet) {
        if (ruleSet == RuleSet.CLASSIC) {
            return Arrays.asList(
                new Attack("Diamond Dust", 0.5, 0.5, 0.5),
                new Attack("Aurora Thunder Attack", 1, 1, 1)
            );
        }

        return Arrays.asList(
            new Attack("Diamond Dust", 2, 8, 6),
            new Attack("Aurora Thunder Attack", 4, 11, 8)
        );
    }

    private static List<ConsumableAttack> consumables(RuleSet ruleSet) {
        if (ruleSet == RuleSet.CLASSIC) {
            return Arrays.asList();
        }

        return Arrays.asList(
            new ConsumableAttack("Freezing Coffin Shard", 0.5, 4),
            new ConsumableAttack("White Bird Gust", 2.0, 9)
        );
    }
}
