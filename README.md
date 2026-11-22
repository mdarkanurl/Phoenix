# Phoenix Alarm

Calm React + Vite web alarm clock with two phone-like alarm cards (morning/night).

## Run

```bash
bun install
bun run dev
```

Open the Vite URL. Production build:

```bash
bun run build
bun run preview
```

## Structure

Web (React + Vite): `src/` — `main.jsx`, `App.jsx`, `alarms.js`, `audio.js`, `components/`, `styles.css`.
Android (Java, minSdk 27): `app/` — Gradle project `PhoenixAlarm`.

## Run

Web: see above. Android:

```bash
cd app && ./gradlew assembleDebug
```

## Features (both)

- Two alarms (light/dark), time picker (5-min steps, AM/PM).
- Day repeat picker with summary.
- 4 sounds with preview.
- Snooze toggle, save/reset, persistence.
- Live ringing with chime, snooze (5 min), stop.
- Responsive (web) / native Android UI.

Alarms ring while app open; browsers may need a click (Save) before audio.
