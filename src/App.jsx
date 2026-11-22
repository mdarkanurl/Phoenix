import { useCallback, useEffect, useRef, useState } from 'react'
import Phone from './components/Phone'
import { DEFAULT_ALARMS, cloneDefaults, formatDays, formatTime } from './alarms'
import { ensureAudio, playAlarm } from './audio'

const STORAGE_KEY = 'phoenix-alarm-settings'

function loadAlarms() {
  try {
    const stored = window.localStorage.getItem(STORAGE_KEY)
    if (!stored) return cloneDefaults()
    const parsed = JSON.parse(stored)
    if (!Array.isArray(parsed)) return cloneDefaults()

    return cloneDefaults().map((fallback) => {
      const saved = parsed.find((alarm) => alarm?.id === fallback.id)
      return saved ? { ...fallback, ...saved, days: [...saved.days] } : fallback
    })
  } catch {
    return cloneDefaults()
  }
}

function RingOverlay({ alarm, onStop, onSnooze }) {
  const time = formatTime(alarm.time)

  return (
    <div className={`ring-overlay ring-overlay-${alarm.theme}`} role="dialog" aria-modal="true">
      <div className="ring-glow" aria-hidden="true" />
      <div className="ring-card">
        <div className="ring-status">
          <span className="ring-dot" />
          Alarm ringing
        </div>

        <div className="ring-clock" aria-label={`${time.hour}:${time.minute} ${time.period}`}>
          {time.hour}:{time.minute}
          <span>{time.period}</span>
        </div>

        <h2>{alarm.name}</h2>
        <p>
          {alarm.sound} · {formatDays(alarm.days)}
        </p>

        <div className="ring-actions">
          <button className="snooze-button" type="button" onClick={onSnooze}>
            Snooze 5 min
          </button>
          <button className="stop-button" type="button" onClick={onStop}>
            Stop
          </button>
        </div>
      </div>
    </div>
  )
}

export default function App() {
  const [savedAlarms, setSavedAlarms] = useState(loadAlarms)
  const [draftAlarms, setDraftAlarms] = useState(loadAlarms)
  const [toasts, setToasts] = useState({})
  const [ringing, setRinging] = useState(null)
  const firedAlarms = useRef(new Set())
  const toastTimers = useRef({})
  const snoozeTimer = useRef(null)

  const showToast = useCallback((id, message) => {
    window.clearTimeout(toastTimers.current[id])
    setToasts((current) => ({ ...current, [id]: message }))
    toastTimers.current[id] = window.setTimeout(() => {
      setToasts((current) => {
        const next = { ...current }
        delete next[id]
        return next
      })
    }, 2400)
  }, [])

  useEffect(() => {
    return () => {
      Object.values(toastTimers.current).forEach((timer) => window.clearTimeout(timer))
      window.clearTimeout(snoozeTimer.current)
    }
  }, [])

  useEffect(() => {
    const checkTime = () => {
      const now = new Date()
      const clock = `${String(now.getHours()).padStart(2, '0')}:${String(
        now.getMinutes(),
      ).padStart(2, '0')}`

      savedAlarms.forEach((alarm) => {
        if (!alarm.days.includes(now.getDay()) || alarm.time !== clock) return

        const occurrence = `${alarm.id}-${now.toDateString()}-${clock}`
        if (firedAlarms.current.has(occurrence)) return
        firedAlarms.current.add(occurrence)
        setRinging(alarm)
      })
    }

    checkTime()
    const interval = window.setInterval(checkTime, 1000)
    return () => window.clearInterval(interval)
  }, [savedAlarms])

  useEffect(() => {
    if (!ringing) return undefined
    ensureAudio()
    playAlarm(ringing.sound)
    const interval = window.setInterval(() => playAlarm(ringing.sound), 2400)

    const handleKeyDown = (event) => {
      if (event.key === 'Escape') setRinging(null)
    }
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      window.clearInterval(interval)
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [ringing])

  const updateAlarm = (id, changes) => {
    setDraftAlarms((current) =>
      current.map((alarm) => (alarm.id === id ? { ...alarm, ...changes } : alarm)),
    )
  }

  const saveAlarm = (id) => {
    ensureAudio()
    const alarm = draftAlarms.find((item) => item.id === id)
    if (!alarm) return

    const nextAlarms = savedAlarms.map((item) => (item.id === id ? alarm : item))
    setSavedAlarms(nextAlarms)
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(nextAlarms))
    showToast(id, 'Alarm saved')
  }

  const resetAlarm = (id) => {
    const defaults = DEFAULT_ALARMS.find((alarm) => alarm.id === id)
    if (!defaults) return
    setDraftAlarms((current) =>
      current.map((alarm) =>
        alarm.id === id ? { ...defaults, days: [...defaults.days] } : alarm,
      ),
    )
    showToast(id, 'Changes reset')
  }

  const snoozeAlarm = () => {
    if (!ringing) return
    const snoozedAlarm = ringing
    setRinging(null)
    showToast(snoozedAlarm.id, 'Snoozed for 5 minutes')
    window.clearTimeout(snoozeTimer.current)
    snoozeTimer.current = window.setTimeout(() => {
      setRinging(snoozedAlarm)
    }, 5 * 60 * 1000)
  }

  return (
    <main className="app">
      <div className="ambient ambient-one" aria-hidden="true" />
      <div className="ambient ambient-two" aria-hidden="true" />

      <section className="phones" aria-label="Alarm settings">
        {draftAlarms.map((alarm) => (
          <Phone
            alarm={alarm}
            key={alarm.id}
            onChange={updateAlarm}
            onSave={saveAlarm}
            onReset={resetAlarm}
            toast={toasts[alarm.id]}
          />
        ))}
      </section>

      {ringing && (
        <RingOverlay
          alarm={ringing}
          onStop={() => setRinging(null)}
          onSnooze={snoozeAlarm}
        />
      )}
    </main>
  )
}
