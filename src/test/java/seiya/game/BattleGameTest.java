package seiya.game;

import org.junit.jupiter.api.Test;
import seiya.actions.Action;
import seiya.actions.Attack;
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
    private static final double DELTA = 0.0001;

    @Test
    void gatherIncreasesSpirit() {
        Seiya seiya = new Seiya();
        double before = seiya.spirit();

        Gather gather = new Gather(2);
        gather.execute(seiya, seiya);

        assertEquals(before + 2, seiya.spirit(), DELTA);
    }

    @Test
    void defendReducesNextHit() {
        Seiya seiya = new Seiya();
        Shiryu shiryu = new Shiryu();

        Defend defend = new Defend(50);
        defend.execute(seiya, shiryu);

        double before = seiya.health();
        Attack hit = new Attack("Test Hit", 0, 8);
        hit.execute(shiryu, seiya);

        assertEquals(before - 4, seiya.health(), DELTA);
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

    @Test
    void simultaneousAttackBlocksWhenAttackIsNotGreaterThanDefense() {
        Seiya seiya = new Seiya();
        Shiryu shiryu = new Shiryu();

        Attack p1Attack = new Attack("P1", 0, 4.5, 4.5);
        Attack p2Attack = new Attack("P2", 0, 4.5, 4.5);

        double p1Before = seiya.health();
        double p2Before = shiryu.health();

        p1Attack.executeForClash(seiya, shiryu, p2Attack.defenseValue(), p1Attack.attackValue() <= p2Attack.defenseValue());
        p2Attack.executeForClash(shiryu, seiya, p1Attack.defenseValue(), p2Attack.attackValue() <= p1Attack.defenseValue());

        assertEquals(p1Before, seiya.health(), DELTA);
        assertEquals(p2Before, shiryu.health(), DELTA);
    }

    @Test
    void turnResolverLogsAttackDefenseAndArmorBreakdown() {
        Player p1 = new Player("P1", new Seiya(), this::simpleAggressiveChoice);
        Player p2 = new Player("P2", new Shiryu(), this::simpleAggressiveChoice);

        p2.character().wearArmorPiece();
        Attack p1Attack = new Attack("Test Hit", 0, 8, 3);
        Action p2Action = new Gather(2);

        String joinedLogs = String.join("\n", TurnResolver.resolve(p1, p1Attack, p2, p2Action));

        assertTrue(joinedLogs.contains("attack=8"));
        assertTrue(joinedLogs.contains("defense=3"));
        assertTrue(joinedLogs.contains("targetArmor=1"));
        assertTrue(joinedLogs.contains("projectedDamage=7"));
    }

    @Test
    void simultaneousDefendUsesDefenseValueToBlockAttack() {
        Player p1 = new Player("P1", new Seiya(), this::simpleAggressiveChoice);
        Player p2 = new Player("P2", new Shiryu(), this::simpleAggressiveChoice);

        Attack attack = new Attack("Test Hit", 0, 8, 2);
        Defend defend = new Defend(50, 8);
        double p2Before = p2.character().health();

        TurnResolver.resolve(p1, attack, p2, defend);

        assertEquals(p2Before, p2.character().health(), DELTA);
    }

    @Test
    void simultaneousGatherRemainsVulnerable() {
        Player p1 = new Player("P1", new Seiya(), this::simpleAggressiveChoice);
        Player p2 = new Player("P2", new Shiryu(), this::simpleAggressiveChoice);

        Attack attack = new Attack("Test Hit", 0, 8, 2);
        Gather gather = new Gather(2);
        double p2Before = p2.character().health();

        TurnResolver.resolve(p1, attack, p2, gather);

        assertEquals(p2Before - 8, p2.character().health(), DELTA);
    }

    @Test
    void simultaneousAttackDealsOnlyAttackMinusOpposingDefense() {
        Player p1 = new Player("P1", new Seiya(), this::simpleAggressiveChoice);
        Player p2 = new Player("P2", new Shiryu(), this::simpleAggressiveChoice);

        Attack attack = new Attack("Test Hit", 0, 10, 2);
        Defend defend = new Defend(50, 4);
        double p2Before = p2.character().health();

        TurnResolver.resolve(p1, attack, p2, defend);

        assertEquals(p2Before - 6, p2.character().health(), DELTA);
    }

    @Test
    void attackCanSpendFractionalSpiritCost() {
        Seiya seiya = new Seiya();
        Shiryu shiryu = new Shiryu();
        Attack attack = new Attack("Fractional Hit", 0.5, 3);

        attack.execute(seiya, shiryu);

        assertEquals(0.5, seiya.spirit(), DELTA);
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
