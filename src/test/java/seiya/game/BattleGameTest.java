package seiya.game;

import org.junit.jupiter.api.Test;
import seiya.actions.Attack;
import seiya.actions.Action;
import seiya.actions.ConsumableAttack;
import seiya.actions.Defend;
import seiya.actions.Gather;
import seiya.actions.WearArmor;
import seiya.characters.Seiya;
import seiya.characters.Shiryu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BattleGameTest {
    @Test
    void gatherIncreasesSpirit() {
        Seiya seiya = new Seiya();
        int before = seiya.spirit();

        Gather gather = new Gather(2);
        gather.execute(seiya, seiya);

        assertEquals(before + 2, seiya.spirit());
    }

    @Test
    void defendReducesNextHit() {
        Seiya seiya = new Seiya();
        Shiryu shiryu = new Shiryu();

        Defend defend = new Defend(50);
        defend.execute(seiya, shiryu);

        int before = seiya.health();
        Attack hit = new Attack("Test Hit", 0, 8);
        hit.execute(shiryu, seiya);

        assertEquals(before - 4, seiya.health());
    }

    @Test
    void wearArmorIncreasesArmorUpToLimit() {
        Seiya seiya = new Seiya();
        WearArmor wearArmor = new WearArmor();

        wearArmor.execute(seiya, seiya);
        wearArmor.execute(seiya, seiya);
        wearArmor.execute(seiya, seiya);
        wearArmor.execute(seiya, seiya);

        assertEquals(3, seiya.armorWorn());
    }

    @Test
    void consumableCanOnlyBeUsedOnce() {
        Seiya seiya = new Seiya();
        Shiryu shiryu = new Shiryu();
        ConsumableAttack consumable = seiya.consumables().get(0);

        consumable.execute(seiya, shiryu);

        assertFalse(seiya.hasConsumable(consumable));
        assertFalse(consumable.canExecute(seiya));
    }

    @Test
    void battleEndsWithWinner() {
        Player p1 = new Player("P1", new Seiya(), this::simpleAggressiveChoice);
        Player p2 = new Player("P2", new Shiryu(), this::simpleAggressiveChoice);

        BattleGame game = new BattleGame(p1, p2, 100);
        String log = game.run();

        assertTrue(log.contains("Winner:") || log.contains("Result: Draw"));
    }

    private Action simpleAggressiveChoice(Player self, Player opponent, java.util.List<Action> available) {
        for (Action action : available) {
            if (action instanceof Attack) {
                return action;
            }
        }
        return available.get(0);
    }
}
