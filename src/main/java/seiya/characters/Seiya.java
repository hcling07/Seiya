package seiya.characters;

import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;

import java.util.Arrays;

public class Seiya extends Character {
    public Seiya() {
        super(
            "Seiya",
            32,
            3,
            1,
            Arrays.asList(
                new Attack("Pegasus Meteor Fist", 2, 7),
                new Attack("Pegasus Comet Fist", 4, 12)
            ),
            Arrays.asList(
                new ConsumableAttack("Athena Dagger", 0, 5),
                new ConsumableAttack("Exploding Chain", 1, 8)
            )
        );
    }
}
