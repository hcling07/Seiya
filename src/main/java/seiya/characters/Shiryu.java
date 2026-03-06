package seiya.characters;

import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;

import java.util.Arrays;

public class Shiryu extends Character {
    public Shiryu() {
        super(
            "Shiryu",
            36,
            4,
            0,
            Arrays.asList(
                new Attack("Rozan Rising Dragon", 3, 10),
                new Attack("Rozan Dragon Flight", 5, 14)
            ),
            Arrays.asList(
                new ConsumableAttack("Dragon Shield Shard", 0, 4),
                new ConsumableAttack("Mountain Crusher", 2, 9)
            )
        );
    }
}
