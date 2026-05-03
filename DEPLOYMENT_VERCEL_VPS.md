# UBAA Web + VPS Deployment

This repository now supports a split deployment:

- `composeApp/build/dist/wasmJs/productionExecutable` for the static Web frontend
- `server/build/libs/server-all.jar` for the long-running Ktor backend

## 1. GitHub and Vercel

Create these GitHub Actions secrets before enabling the workflow:

- `API_ENDPOINT`: public backend base URL, for example `https://ubaa-api.example.com`
- `VERCEL_TOKEN`
- `VERCEL_ORG_ID`
- `VERCEL_PROJECT_ID`

Workflow:

- `.github/workflows/deploy-vercel.yml`
- pushes to `main` deploy production
- pushes to `feature/**` deploy preview

The static deployment includes `vercel.json` from `composeApp/src/webMain/resources/vercel.json`.

## 2. VPS backend

Install:

- JDK 21
- Redis with persistence enabled

Required environment variables:

- `JWT_SECRET`
- `REDIS_URI`
- `SERVER_PORT`
- `CORS_ALLOWED_ORIGINS`
- `WEB_PUSH_VAPID_PUBLIC_KEY`
- `WEB_PUSH_VAPID_PRIVATE_KEY`

Recommended example:

```bash
export JWT_SECRET='replace-me'
export REDIS_URI='redis://127.0.0.1:6379'
export SERVER_PORT='5432'
export CORS_ALLOWED_ORIGINS='https://your-vercel-domain.vercel.app'
export WEB_PUSH_VAPID_PUBLIC_KEY='replace-me'
export WEB_PUSH_VAPID_PRIVATE_KEY='replace-me'
java -jar server-all.jar
```

## 3. systemd example

```ini
[Unit]
Description=UBAA Ktor Server
After=network.target redis.service

[Service]
WorkingDirectory=/opt/ubaa
ExecStart=/usr/bin/java -jar /opt/ubaa/server-all.jar
Restart=always
RestartSec=5
Environment=JWT_SECRET=replace-me
Environment=REDIS_URI=redis://127.0.0.1:6379
Environment=SERVER_PORT=5432
Environment=CORS_ALLOWED_ORIGINS=https://your-vercel-domain.vercel.app
Environment=WEB_PUSH_VAPID_PUBLIC_KEY=replace-me
Environment=WEB_PUSH_VAPID_PRIVATE_KEY=replace-me

[Install]
WantedBy=multi-user.target
```

## 4. Local verification

Commands used for this feature branch:

```bash
./gradlew :server:test :shared:jvmTest :composeApp:jvmTest
./gradlew :server:buildFatJar :composeApp:wasmJsBrowserDistribution
```

Runtime smoke checks:

- `GET /health/ready` returns Redis-ready status
- static frontend can be served directly from the Wasm distribution directory
