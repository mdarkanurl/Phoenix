# Phoenix Alarm

A calm alarm clock web app built with React and Vite, inspired by the light/dark mobile alarm designs in the reference image.

## Run it

```bash
bun install
bun run dev
```

Open the local URL printed by Vite. To build for production:

```bash
bun run build
bun run preview
```

## Features

- **Two independent alarms** — a warm morning alarm and a cool night alarm, shown side by side.
- **Circular alarm cards** — tap the time to open a custom time picker with AM/PM and five-minute steps.
- **Day repeat picker** — toggle any day; the repeat summary updates automatically.
- **Alarm sounds** — choose from four gentle tones with an instant audio preview.
- **Snooze toggle** — per-alarm snooze preference.
- **Save and reset** — saves each alarm to `localStorage`; reset restores that alarm's defaults.
- **Live alarm ringing** — when a saved alarm matches the current time and day, a ringing screen appears with a chime, snooze, and stop controls.
- **Responsive layout** — the two phones sit side by side on desktop and stack on smaller screens.

Alarms ring while the page is open. Browsers may require a click (for example, pressing **Save**) before audio can play.
