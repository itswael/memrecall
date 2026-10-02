# MemRecall

A configurable Android flashcard app built for technical interview prep — system design, LLD, DSA, and core CS subjects. Powered by the SM-2 spaced repetition algorithm, swipe-based review, and per-subject smart notifications.

---

## Features

### Study Modes
| Mode | Description |
|------|-------------|
| **Revision** | SM-2 queue — due cards first, then new, then upcoming |
| **Learning** | Browse all cards in the subject |
| **Quick** | 20 random cards from any subject |

### Card Templates
| Type | Fields |
|------|--------|
| **General** | Front / Back / Hint |
| **Theory** | Concept · Explanation · Key points · Mnemonic · Example |
| **DSA** | Problem · Naive approach + complexity · Optimised approach + complexity · Key insight · Pseudocode · Follow-ups · Company tags |
| **System Design** | Requirements · HLD · Components · Trade-offs · Scaling notes · Bottlenecks |

### Spaced Repetition (SM-2)
- Cards are rated **Again / Hard / Good / Easy** after each review
- Interval and easiness factor adjust automatically — a card you keep forgetting appears daily; one you know well might not surface for weeks
- Difficulty badge (Easy / Medium / Hard) is computed from recall history

### Swipe UX
- Swipe **right** → remembered (quality 5 — Easy)
- Swipe **left** → forgot (quality 1 — Again)
- Live colour overlay fades in during drag; haptic feedback fires on threshold
- Tap card to flip with a smooth 3D `rotationY` animation

### Subjects & Notifications
- Create subjects with a custom colour and icon
- Per-subject notification schedule with three modes:
  - **Fixed** — fire at a set time every day
  - **Random** — pick a random moment within your study window
  - **Smart** — fire on a configurable interval, clamped to your window
- Alarms survive device reboots via `BOOT_COMPLETED`

### Stats
- Today's study minutes, total sessions, best-ever streak
- Per-session recall rate with colour coding
- Subject progress rings (mastered / total)

---

## Architecture

```
┌─────────────────────────────────────────────┐
│  Presentation  (Jetpack Compose + ViewModel) │
│  Home · SubjectDetail · Study · Stats · …   │
├─────────────────────────────────────────────┤
│  Domain  (pure Kotlin)                       │
│  Models · Repository interfaces · SM-2       │
├─────────────────────────────────────────────┤
│  Data                                        │
│  Room (entities + DAOs) · Gson mapper        │
│  AlarmManager · Google Drive (WIP)           │
└─────────────────────────────────────────────┘
```

Clean MVVM — ViewModels observe Flows from repositories; the UI only knows about domain models.  
Card content is stored as typed JSON so new templates can be added without a schema migration.

---

## Tech Stack

| Layer | Libraries |
|-------|-----------|
| UI | Jetpack Compose · Material Design 3 |
| Navigation | Navigation Compose |
| DI | Hilt |
| Database | Room |
| Async | Kotlin Coroutines · Flow |
| Notifications | AlarmManager · WorkManager |
| Charts | Vico |
| Animations | Lottie |
| Backup | Google Drive API *(scaffolded)* |
| Serialisation | Gson |

---

## Getting Started

1. Clone the repo
   ```bash
   git clone https://github.com/itswael/memrecall.git
   ```
2. Open in **Android Studio Ladybug** (or newer)
3. Let Gradle sync — no API keys needed to run locally
4. Run on a device or emulator (minSdk 26 / Android 8.0)

### Optional: Google Drive backup
Add your `google-services.json` (from Firebase Console) to `app/` and uncomment the Drive auth flow in `SettingsScreen`.

---

## Roadmap

- [ ] Google Drive backup & restore
- [ ] JSON / CSV bulk import from file picker
- [ ] DataStore persistence for daily goal and theme preference
- [ ] Study heatmap calendar on the Stats screen
- [ ] Markdown rendering in card content
- [ ] Code syntax highlighting for DSA pseudocode
- [ ] Gemini API hint button ("show me a nudge")
- [ ] Streak increment logic post-session
- [ ] Widget for due-card count on home screen

---

## Project Structure

```
app/src/main/java/com/memrecall/
├── data/
│   ├── local/          # Room entities, DAOs, AppDatabase
│   ├── mapper/         # Entity ↔ domain model conversions
│   └── repository/     # Repository implementations
├── domain/
│   ├── algorithm/      # SM-2 spaced repetition
│   ├── model/          # Domain models + card content sealed class
│   └── repository/     # Repository interfaces
├── di/                 # Hilt modules
├── notification/       # AlarmManager scheduling + BroadcastReceiver
└── ui/
    ├── components/     # Shared Compose components
    ├── navigation/     # NavGraph + Screen routes
    ├── screens/        # Feature screens + ViewModels
    └── theme/          # Colour, typography, shapes
```

---

## License

MIT
