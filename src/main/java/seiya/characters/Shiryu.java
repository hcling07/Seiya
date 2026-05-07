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
                new Attack("Rozan Dragon Flight", 2, 2, 6),
                new Attack("Rozan Hundred Dragon", 3, 3, 3),
                new Attack("Rozan Hyper Dragon", 4, 1, 10),
                new Attack("Cosmo Explosion", 5, 5, 5),
                new Attack("Rozan Dragon Roar", 6, 6, 6)
            );
        }

        // TODO: Add Shiryu's new classic attacks to the default rule set once their default-scale values are decided.
        return Arrays.asList(
            new Attack("Rozan Rising Dragon", 2, 9),
            new Attack("Rozan Dragon Flight", 3, 13),
            new Attack("Cosmo Explosion", 5, 15)
        );
    }

    private static List<ConsumableAttack> consumables(RuleSet ruleSet) {
        if (ruleSet == RuleSet.CLASSIC) {
            return Arrays.asList(
                new ConsumableAttack("Dragon Shield Shard", 0, 4.5, 4.5)
            );
        }

        return Arrays.asList(
            new ConsumableAttack("Dragon Shield Shard", 0, 4),
            new ConsumableAttack("Mountain Crusher", 2, 9)
        );
    }
}
