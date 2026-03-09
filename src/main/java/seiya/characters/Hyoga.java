package seiya.characters;

import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;

import java.util.Arrays;

public class Hyoga extends Character {
    public Hyoga() {
        super(
            "Hyoga",
            34,
            3,
            1,
            Arrays.asList(
                new Attack("Diamond Dust", 2, 8, 6),
                new Attack("Aurora Thunder Attack", 4, 11, 8)
            ),
            Arrays.asList(
                new ConsumableAttack("Freezing Coffin Shard", 0.5, 4),
                new ConsumableAttack("White Bird Gust", 2.0, 9)
            )
        );
    }
}
