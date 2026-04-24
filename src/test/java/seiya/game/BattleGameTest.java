package seiya.game;

import org.junit.jupiter.api.Test;
import seiya.actions.Action;
import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;
import seiya.actions.Defend;
import seiya.actions.Gather;
import seiya.actions.WearArmor;
import seiya.characters.Hyoga;
import seiya.characters.Seiya;
import seiya.characters.Shiryu;
import seiya.controllers.BasicAiController;
import seiya.controllers.rules.DefendAgainstLikelyAttackRule;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
    void armorBreaksWhenAttackPenetratesIt() {
        Seiya seiya = new Seiya();
        Shiryu shiryu = new Shiryu();
        seiya.wearArmorPiece();
        seiya.wearArmorPiece();

        Attack hit = new Attack("Test Hit", 0, 5);
        hit.execute(shiryu, seiya);

        assertEquals(1, seiya.armorWorn());
    }

    @Test
    void armorStaysWhenAttackDoesNotPenetrateIt() {
        Seiya seiya = new Seiya();
        Shiryu shiryu = new Shiryu();
        seiya.wearArmorPiece();
        seiya.wearArmorPiece();

        Attack hit = new Attack("Light Hit", 0, 2);
        hit.execute(shiryu, seiya);

        assertEquals(2, seiya.armorWorn());
    }

    @Test
    void brokenArmorDoesNotAllowExtraWearBeyondTotalArmor() {
        Seiya seiya = new Seiya();
        Shiryu shiryu = new Shiryu();
        WearArmor wearArmor = new WearArmor();
        Attack hit = new Attack("Break Armor", 0, 10);

        wearArmor.execute(seiya, seiya);
        hit.execute(shiryu, seiya);

        wearArmor.execute(seiya, seiya);
        wearArmor.execute(seiya, seiya);
        wearArmor.execute(seiya, seiya);

        assertEquals(2, seiya.armorWorn());
        assertFalse(seiya.canWearArmor());
    }

    @Test
    void consumableCanOnlyBeUsedOnce() {
        Seiya seiya = new Seiya();
        Shiryu shiryu = new Shiryu();
        ConsumableAttack consumable = seiya.consumables().get(0);

        seiya.wearArmorPiece();
        consumable.execute(seiya, shiryu);

        assertFalse(seiya.hasConsumable(consumable));
        assertFalse(consumable.canExecute(seiya));
    }

    @Test
    void consumableRequiresArmorToBeEquipped() {
        Seiya seiya = new Seiya();
        ConsumableAttack consumable = seiya.consumables().get(0);

        assertFalse(consumable.canExecute(seiya));

        seiya.wearArmorPiece();

        assertTrue(consumable.canExecute(seiya));
    }

    @Test
    void consumableStaysAvailableAfterArmorBreaks() {
        Seiya seiya = new Seiya();
        Shiryu shiryu = new Shiryu();
        ConsumableAttack consumable = seiya.consumables().get(0);

        seiya.wearArmorPiece();
        Attack hit = new Attack("Heavy Hit", 0, 6);
        hit.execute(shiryu, seiya);

        assertEquals(0, seiya.armorWorn());
        assertTrue(consumable.canExecute(seiya));
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
    void newCharacterCanParticipateInBattle() {
        Player p1 = new Player("P1", new Hyoga(), this::simpleAggressiveChoice);
        Player p2 = new Player("P2", new Shiryu(), this::simpleAggressiveChoice);

        BattleGame game = new BattleGame(p1, p2, 20);
        String log = game.run();

        assertTrue(log.contains("Hyoga"));
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
    void simultaneousTurnsStillResolveAfterFirstPlayerDefeatsOpponent() {
        Player p1 = new Player("P1", new Seiya(), this::simpleAggressiveChoice);
        Player p2 = new Player("P2", new Shiryu(), this::simpleAggressiveChoice);

        Attack lethal = new Attack("Lethal", 0, 100, 0);
        Gather gather = new Gather(2);

        TurnResolver.resolve(p1, lethal, p2, gather);

        assertEquals(2.0, p2.character().spirit(), DELTA);
    }

    @Test
    void attackCanSpendFractionalSpiritCost() {
        Seiya seiya = new Seiya();
        Shiryu shiryu = new Shiryu();
        Attack attack = new Attack("Fractional Hit", 0.5, 3);
        seiya.gainSpirit(1.0);

        attack.execute(seiya, shiryu);

        assertEquals(0.5, seiya.spirit(), DELTA);
    }

    @Test
    void classicGatherIncreasesSpiritByOne() {
        Player player = new Player("P1", new Seiya(RuleSet.CLASSIC), new BasicAiController());
        double before = player.character().spirit();
        Gather gather = findAction(player, Gather.class);

        gather.execute(player.character(), player.character());

        assertEquals(before + 1, player.character().spirit(), DELTA);
    }

    @Test
    void charactersStartWithZeroSpirit() {
        assertEquals(0.0, new Seiya().spirit(), DELTA);
        assertEquals(0.0, new Shiryu().spirit(), DELTA);
        assertEquals(0.0, new Hyoga().spirit(), DELTA);
    }

    @Test
    void classicDefendAndWearArmorUseFiveDefenseValue() {
        Player player = new Player("P1", new Seiya(RuleSet.CLASSIC), new BasicAiController());
        Player opponent = new Player("P2", new Shiryu(RuleSet.CLASSIC), new BasicAiController());
        Defend defend = findAction(player, Defend.class);

        TurnResolver.resolve(player, findAction(player, Gather.class), opponent, findAction(opponent, Gather.class));

        WearArmor wearArmor = findAction(player, WearArmor.class);

        assertEquals(5.0, defend.defenseValue(), DELTA);
        assertEquals(5.0, wearArmor.defenseValue(), DELTA);
    }

    @Test
    void classicWearArmorStartsLockedUntilAfterFirstTurn() {
        Player p1 = new Player("P1", new Seiya(RuleSet.CLASSIC), this::simpleAggressiveChoice);
        Player p2 = new Player("P2", new Shiryu(RuleSet.CLASSIC), this::simpleAggressiveChoice);

        assertFalse(hasAction(p1, WearArmor.class));

        TurnResolver.resolve(p1, findAction(p1, Gather.class), p2, findAction(p2, Gather.class));

        assertTrue(hasAction(p1, WearArmor.class));
        assertTrue(hasAction(p2, WearArmor.class));
    }

    @Test
    void classicConsumablesCostZeroSpiritAfterArmorUnlock() {
        Seiya seiya = new Seiya(RuleSet.CLASSIC);
        ConsumableAttack consumable = seiya.consumables().get(0);

        assertFalse(consumable.canExecute(seiya));

        seiya.wearArmorPiece();

        assertEquals(0.0, consumable.spiritCost(), DELTA);
        assertTrue(consumable.canExecute(seiya));
    }

    @Test
    void classicAttackWithoutArmorDefeatsCharacter() {
        Seiya seiya = new Seiya(RuleSet.CLASSIC);
        Shiryu shiryu = new Shiryu(RuleSet.CLASSIC);
        Attack hit = new Attack("Classic Hit", 0, 1);

        hit.execute(shiryu, seiya);

        assertFalse(seiya.isAlive());
    }

    @Test
    void classicAttackCanReduceArmorToZeroWithoutDefeat() {
        Seiya seiya = new Seiya(RuleSet.CLASSIC);
        Shiryu shiryu = new Shiryu(RuleSet.CLASSIC);
        seiya.wearArmorPiece();
        Attack hit = new Attack("Classic Hit", 0, 1);

        hit.execute(shiryu, seiya);

        assertEquals(0, seiya.armorWorn());
        assertTrue(seiya.isAlive());
    }

    @Test
    void classicAttackAtFiveStripsTwoArmor() {
        Player p1 = new Player("P1", new Shiryu(RuleSet.CLASSIC), this::simpleAggressiveChoice);
        Player p2 = new Player("P2", new Hyoga(RuleSet.CLASSIC), this::simpleAggressiveChoice);

        p2.character().wearArmorPiece();
        p2.character().wearArmorPiece();

        Attack attack = new Attack("Classic Hit", 0, 6, 6);
        Defend defend = new Defend(0, 1);

        TurnResolver.resolve(p1, attack, p2, defend);

        assertEquals(0, p2.character().armorWorn());
        assertTrue(p2.character().isAlive());
    }

    @Test
    void basicAiDoesNotDefendWhenOpponentCannotAttack() {
        Player player = new Player("P1", new Seiya(), new BasicAiController());
        Player opponent = new Player("P2", new Hyoga(), new BasicAiController());

        Action action = player.chooseAction(opponent);

        assertTrue(action instanceof Gather);
    }

    @Test
    void basicAiDoesNotDefendWhenProbabilityRollMissesAttackLikelihood() {
        Player player = new Player(
            "P1",
            new Seiya(),
            new BasicAiController(Arrays.asList(new DefendAgainstLikelyAttackRule(() -> 0.40)))
        );
        Player opponent = new Player("P2", new Hyoga(), new BasicAiController());
        opponent.character().gainSpirit(4.0);

        Action action = player.chooseAction(opponent);

        assertTrue(action instanceof Gather);
    }

    @Test
    void basicAiDefendsWhenProbabilityRollHitsAttackLikelihood() {
        Player player = new Player(
            "P1",
            new Seiya(),
            new BasicAiController(Arrays.asList(new DefendAgainstLikelyAttackRule(() -> 0.39)))
        );
        Player opponent = new Player("P2", new Hyoga(), new BasicAiController());
        opponent.character().gainSpirit(4.0);

        Action action = player.chooseAction(opponent);

        assertTrue(action instanceof Defend);
    }

    @Test
    void basicAiDefendRuleCanBeDisabled() {
        Player player = new Player("P1", new Seiya(), new BasicAiController(Collections.emptyList()));
        Player opponent = new Player("P2", new Hyoga(), new BasicAiController());
        opponent.character().gainSpirit(2.0);

        Action action = player.chooseAction(opponent);

        assertTrue(action instanceof Gather);
    }

    @Test
    void basicAiDoesNotRepeatDefendWhileDefenseIsActive() {
        Player player = new Player(
            "P1",
            new Seiya(),
            new BasicAiController(Arrays.asList(new DefendAgainstLikelyAttackRule(() -> 0.0)))
        );
        Player opponent = new Player("P2", new Hyoga(), new BasicAiController());
        opponent.character().gainSpirit(4.0);
        new Defend(50).execute(player.character(), opponent.character());

        Action action = player.chooseAction(opponent);

        assertTrue(action instanceof Gather);
    }

    @Test
    void classicAiWearsArmorInsteadOfDefendingWhenThreatened() {
        Player player = new Player(
            "P1",
            new Seiya(RuleSet.CLASSIC),
            new BasicAiController(Arrays.asList(new DefendAgainstLikelyAttackRule(() -> 0.0)))
        );
        Player opponent = new Player("P2", new Shiryu(RuleSet.CLASSIC), new BasicAiController());
        player.recordTurn();
        player.character().gainSpirit(1.0);
        opponent.character().gainSpirit(1.0);

        Action action = player.chooseAction(opponent);

        assertTrue(action instanceof WearArmor);
    }

    @Test
    void classicAiDefendsWhenThreatenedAndArmorIsUnavailable() {
        Player player = new Player(
            "P1",
            new Hyoga(RuleSet.CLASSIC),
            new BasicAiController(Arrays.asList(new DefendAgainstLikelyAttackRule(() -> 0.0)))
        );
        Player opponent = new Player("P2", new Shiryu(RuleSet.CLASSIC), new BasicAiController());
        player.recordTurn();
        player.character().wearArmorPiece();
        player.character().wearArmorPiece();
        opponent.character().gainSpirit(1.0);

        Action action = player.chooseAction(opponent);

        assertTrue(action instanceof Defend);
    }

    @Test
    void basicAiCanForceDefendWithLowProbabilityRoll() {
        Player player = new Player(
            "P1",
            new Seiya(),
            new BasicAiController(Arrays.asList(new DefendAgainstLikelyAttackRule(() -> 0.0)))
        );
        Player opponent = new Player("P2", new Hyoga(), new BasicAiController());
        opponent.character().gainSpirit(2.0);

        Action action = player.chooseAction(opponent);

        assertTrue(action instanceof Defend);
    }

    private Action simpleAggressiveChoice(Player self, Player opponent, java.util.List<Action> available) {
        for (Action action : available) {
            if (action instanceof Attack) {
                return action;
            }
        }
        return available.get(0);
    }

    private <T extends Action> T findAction(Player player, Class<T> actionType) {
        for (Action action : player.availableActions()) {
            if (actionType.isInstance(action)) {
                return actionType.cast(action);
            }
        }
        fail("Action not found: " + actionType.getSimpleName());
        return actionType.cast(player.availableActions().get(0));
    }

    private boolean hasAction(Player player, Class<? extends Action> actionType) {
        for (Action action : player.availableActions()) {
            if (actionType.isInstance(action)) {
                return true;
            }
        }
        return false;
    }
}
