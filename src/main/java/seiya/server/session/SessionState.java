package seiya.server.session;

import java.util.List;

public final class SessionState {
    private final String roomCode;
    private final String ruleSet;
    private final String status;
    private final int requesterSlot;
    private final boolean battleEnded;
    private final String resultText;
    private final List<PlayerState> players;
    private final List<String> log;

    public SessionState(
        String roomCode,
        String ruleSet,
        String status,
        int requesterSlot,
        boolean battleEnded,
        String resultText,
        List<PlayerState> players,
        List<String> log
    ) {
        this.roomCode = roomCode;
        this.ruleSet = ruleSet;
        this.status = status;
        this.requesterSlot = requesterSlot;
        this.battleEnded = battleEnded;
        this.resultText = resultText;
        this.players = players;
        this.log = log;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getRuleSet() {
        return ruleSet;
    }

    public String getStatus() {
        return status;
    }

    public int getRequesterSlot() {
        return requesterSlot;
    }

    public boolean isBattleEnded() {
        return battleEnded;
    }

    public String getResultText() {
        return resultText;
    }

    public List<PlayerState> getPlayers() {
        return players;
    }

    public List<String> getLog() {
        return log;
    }

    public static final class PlayerState {
        private final int slot;
        private final boolean you;
        private final String playerName;
        private final String character;
        private final String orientation;
        private final boolean alive;
        private final boolean pending;
        private final boolean winner;
        private final double health;
        private final double maxHealth;
        private final double spirit;
        private final int armorWorn;
        private final int remainingArmor;
        private final int defendPercent;
        private final List<ActionState> availableActions;

        public PlayerState(
            int slot,
            boolean you,
            String playerName,
            String character,
            String orientation,
            boolean alive,
            boolean pending,
            boolean winner,
            double health,
            double maxHealth,
            double spirit,
            int armorWorn,
            int remainingArmor,
            int defendPercent,
            List<ActionState> availableActions
        ) {
            this.slot = slot;
            this.you = you;
            this.playerName = playerName;
            this.character = character;
            this.orientation = orientation;
            this.alive = alive;
            this.pending = pending;
            this.winner = winner;
            this.health = health;
            this.maxHealth = maxHealth;
            this.spirit = spirit;
            this.armorWorn = armorWorn;
            this.remainingArmor = remainingArmor;
            this.defendPercent = defendPercent;
            this.availableActions = availableActions;
        }

        public int getSlot() { return slot; }
        public boolean isYou() { return you; }
        public String getPlayerName() { return playerName; }
        public String getCharacter() { return character; }
        public String getOrientation() { return orientation; }
        public boolean isAlive() { return alive; }
        public boolean isPending() { return pending; }
        public boolean isWinner() { return winner; }
        public double getHealth() { return health; }
        public double getMaxHealth() { return maxHealth; }
        public double getSpirit() { return spirit; }
        public int getArmorWorn() { return armorWorn; }
        public int getRemainingArmor() { return remainingArmor; }
        public int getDefendPercent() { return defendPercent; }
        public List<ActionState> getAvailableActions() { return availableActions; }
    }

    public static final class ActionState {
        private final String id;
        private final String name;
        private final String category;
        private final double attack;
        private final double defense;
        private final double spiritCost;

        public ActionState(String id, String name, String category, double attack, double defense, double spiritCost) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.attack = attack;
            this.defense = defense;
            this.spiritCost = spiritCost;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getAttack() { return attack; }
        public double getDefense() { return defense; }
        public double getSpiritCost() { return spiritCost; }
    }
}
