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
- `web` for the web-based multiplayer server

## Run Web Multiplayer Locally

Start the web server from the project root:

```bash
./gradlew run --args="web 8080"
```

Open the game on the same machine:

```text
http://localhost:8080
```

Keep this terminal running while playing. You can verify the local server is available with:

```bash
curl -I http://localhost:8080
```

## Access Web Multiplayer Remotely

Cloudflare Quick Tunnel provides a temporary public HTTPS address without requiring a domain or router port forwarding.

Install `cloudflared` on macOS:

```bash
brew install cloudflared
```

Start the local web server in the first terminal:

```bash
cd /Users/Peiran/IdeaProjects/Seiya
./gradlew run --args="web 8080"
```

Start the Cloudflare tunnel in a second terminal:

```bash
cloudflared tunnel --url http://localhost:8080 --no-autoupdate
```

Cloudflare prints a temporary address similar to:

```text
https://random-words.trycloudflare.com
```

Open that address from any remote device or send it to the other player. Both the Java server and Cloudflare tunnel terminals must remain running. A new temporary address is generated whenever the tunnel is restarted.

## Stop Web Hosting

When the processes are running in their terminal windows, press `Ctrl+C` in each terminal:

1. Stop the Cloudflare tunnel.
2. Stop the Java web server.

If the original terminals are unavailable, locate the processes:

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
ps -ax | grep '[c]loudflared tunnel'
```

Stop them using their process IDs:

```bash
kill <java-pid>
kill <cloudflared-pid>
```

You can also stop them by command:

```bash
pkill -f 'seiya.Main web'
pkill -f 'cloudflared tunnel'
```

## Balance Simulation

Run the classic-rule character balance simulation from the project root:

```bash
./gradlew -q testClasses
java -cp build/classes/java/main:build/classes/java/test seiya.sim.ClassicBalanceSimulation
```

Optionally pass the number of matches per ordered matchup:

```bash
java -cp build/classes/java/main:build/classes/java/test seiya.sim.ClassicBalanceSimulation 5000
```
