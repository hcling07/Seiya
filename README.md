# Seiya

## Run UI

Use these commands from the project root:

```bash
./gradlew -q classes
java -cp build/classes/java/main seiya.Main ui
```

The UI start screen lets you choose:

- Human character
- AI character
- Rule set: `Default` or `Classic`

## Other Modes

`Main` also supports:

- `ava` for AI vs AI using default rules
- `ava classic` for AI vs AI using classic rules
