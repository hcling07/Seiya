package seiya.characters;

import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;
import seiya.game.RuleSet;

import java.util.Arrays;
import java.util.List;

public class Seiya extends Character {
    public Seiya() {
        this(RuleSet.DEFAULT);
    }

    public Seiya(RuleSet ruleSet) {
        super(
            ruleSet,
            "Seiya",
            32,
            ruleSet == RuleSet.CLASSIC ? 1 : 3,
            0,
            attacks(ruleSet),
            consumables(ruleSet)
        );
    }

    private static List<Attack> attacks(RuleSet ruleSet) {
        if (ruleSet == RuleSet.CLASSIC) {
            return Arrays.asList(
                new Attack("Pegasus Meteor Fist", 1, 1, 1),
                new Attack("Pegasus Comet Fist", 2, 2, 2)
            );
        }

        return Arrays.asList(
            new Attack("Pegasus Meteor Fist", 2, 7),
            new Attack("Pegasus Comet Fist", 4, 12)
        );
    }

    private static List<ConsumableAttack> consumables(RuleSet ruleSet) {
        if (ruleSet == RuleSet.CLASSIC) {
            return Arrays.asList(
                new ConsumableAttack("Athena Dagger", 0, 4.5, 4.5),
                new ConsumableAttack("Exploding Chain", 0, 4.5, 4.5)
            );
        }

        return Arrays.asList(
            new ConsumableAttack("Athena Dagger", 0, 5),
            new ConsumableAttack("Exploding Chain", 1, 8)
        );
    }
}
