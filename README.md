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

The server also exposes a health check:

```text
http://localhost:8080/health
```

## Run Web Multiplayer with Docker

Build the same container image used by Render:

```bash
docker build -t seiya-web .
```

Run the container locally:

```bash
docker run --rm -p 8080:10000 -e PORT=10000 seiya-web
```

Open the game:

```text
http://localhost:8080
```

Press `Ctrl+C` to stop the container.

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

## Deploy to Render

Render runs the Java server on its own infrastructure and provides a stable `onrender.com` HTTPS address. Your local computer does not need to remain online.

The repository includes:

- `Dockerfile` for building and running the Java web server
- `render.yaml` for the Render service configuration
- `/health` for Render health checks
- support for Render's `PORT` environment variable

Deploy after committing and pushing the files:

1. Sign in to [Render](https://dashboard.render.com/).
2. Select **New** and then **Blueprint**.
3. Connect the Git repository.
4. Select the branch containing `render.yaml`.
5. Review the `seiya-multiplayer` web service and apply the Blueprint.
6. Wait for the Docker build and health check to complete.
7. Open the generated address:

```text
https://seiya-multiplayer.onrender.com
```

The exact service name and generated address might differ if the name is already taken.

Each push to the connected branch can trigger an automatic rebuild and deployment.

Rooms currently exist only in the running Java process. A Render restart, deployment, or free-instance spin-down removes all active rooms. Keep the service at one instance until room state is moved to shared persistent storage.

## Architecture

The Java repository owns the shared game engine and authoritative server:

```text
seiya.actions / characters / game / controllers
  Shared combat rules and AI

seiya.server.session
  Room lifecycle, players, turns, and typed session snapshots

seiya.server.api
  Versioned API requests, responses, and use cases

seiya.server.transport
  HTTP routing, JSON serialization, static files, and assets
```

The desktop Swing UI calls the shared Java game engine directly. Web, Android, and iOS clients call the authoritative server over HTTPS.

A separate repository is recommended for each native client:

```text
Seiya
  Java game core, server, desktop UI, and bundled web client

Seiya-Android
  Android UI, local client state, and API client

Seiya-iOS
  iOS UI, local client state, and API client
```

Keep the Java core and server together until the core needs independent releases or is shared by multiple Java services. Splitting them now would add versioning and publishing work without helping mobile clients, which consume the HTTP API rather than Java packages.

## Server API

New clients should use the versioned `/api/v1` routes:

```text
GET  /api/v1/options
POST /api/v1/rooms
POST /api/v1/rooms/{roomCode}/join
GET  /api/v1/rooms/{roomCode}
POST /api/v1/rooms/{roomCode}/actions
POST /api/v1/rooms/{roomCode}/rematch
POST /api/v1/rooms/{roomCode}/exit
```

`POST` requests accept JSON. For example:

```json
{
  "character": "SEIYA",
  "ruleSet": "DEFAULT"
}
```

Authenticated room requests can provide the player token with:

```text
Authorization: Bearer <player-token>
```

The original unversioned form endpoints remain available for compatibility, but new mobile clients should use JSON and `/api/v1`.

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
