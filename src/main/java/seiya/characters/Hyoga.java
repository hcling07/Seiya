package seiya.characters;

import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;
import seiya.actions.FreezingCoffin;
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
                new Attack("Aurora Thunder Attack", 1, 1, 1),
                new Attack("Kholodnyi Smerch", 2, 2, 2),
                new Attack("Cosmo Explosion", 5, 5, 5),
                new Attack("Aurora Strike", 6, 6, 6),
                new Attack("Aurora Lightning", 8, 8, 8),
                new Attack("Aurora Execution", 12, 12, 12)
            );
        }

        // TODO: Add Kholodnyi Smerch and Hyoga's higher-cost attacks to the default rule set once their default-scale values are decided.
        return Arrays.asList(
            new Attack("Diamond Dust", 2, 8, 6),
            new Attack("Aurora Thunder Attack", 4, 11, 8),
            new Attack("Cosmo Explosion", 5, 15)
        );
    }

    private static List<ConsumableAttack> consumables(RuleSet ruleSet) {
        if (ruleSet == RuleSet.CLASSIC) {
            return Arrays.asList(
                new FreezingCoffin(),
                new ConsumableAttack("Aurora Execution", 0, 4.5, 4.5)
            );
        }

        return Arrays.asList(
            new ConsumableAttack("Freezing Coffin Shard", 0.5, 4),
            new ConsumableAttack("White Bird Gust", 2.0, 9)
        );
    }
}
