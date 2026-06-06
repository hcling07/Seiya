package seiya.game;

import seiya.characters.Character;
import seiya.characters.Hyoga;
import seiya.characters.Seiya;
import seiya.characters.Shiryu;

public enum CharacterType {
    SEIYA("Seiya") {
        @Override
        public Character create(RuleSet ruleSet) {
            return new Seiya(ruleSet);
        }
    },
    SHIRYU("Shiryu") {
        @Override
        public Character create(RuleSet ruleSet) {
            return new Shiryu(ruleSet);
        }
    },
    HYOGA("Hyoga") {
        @Override
        public Character create(RuleSet ruleSet) {
            return new Hyoga(ruleSet);
        }
    };

    private final String label;

    CharacterType(String label) {
        this.label = label;
    }

    public abstract Character create(RuleSet ruleSet);

    public static CharacterType fromLabel(String value) {
        if (value == null || value.trim().isEmpty()) {
            return SEIYA;
        }

        String normalized = value.trim().replace('-', '_').replace(' ', '_').toUpperCase();
        for (CharacterType type : values()) {
            if (type.name().equals(normalized) || type.label.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported character: " + value);
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
