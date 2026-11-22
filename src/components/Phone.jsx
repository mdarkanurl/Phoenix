import { useEffect, useRef, useState } from 'react'
import {
  DAY_LABELS,
  DAY_NAMES,
  SOUNDS,
  formatDays,
  formatTime,
  fromPickerTime,
  toPickerTime,
} from '../alarms'
import { previewSound } from '../audio'
import {
  CheckIcon,
  ChevronDown,
  ChevronRight,
  ChevronUp,
  Moon,
  MusicNote,
  StatusBar,
  Zzz,
} from './icons'

const RING_RADIUS = 104
const RING_CIRCUMFERENCE = 2 * Math.PI * RING_RADIUS
const RING_START_ANGLE = 135
const KNOB_X = 120 + RING_RADIUS * Math.cos((RING_START_ANGLE * Math.PI) / 180)
const KNOB_Y = 120 + RING_RADIUS * Math.sin((RING_START_ANGLE * Math.PI) / 180)

function ClockRing({ alarm, onOpenTime }) {
  const time = formatTime(alarm.time)
  const isLight = alarm.theme === 'light'

  return (
    <div className="clock-ring">
      <svg className="ring-svg" viewBox="0 0 240 240" aria-hidden="true">
        <circle className="ring-track" cx="120" cy="120" r={RING_RADIUS} />
        <circle
          className="ring-progress"
          cx="120"
          cy="120"
          r={RING_RADIUS}
          strokeDasharray={`${RING_CIRCUMFERENCE * 0.765} ${RING_CIRCUMFERENCE}`}
          transform={`rotate(${RING_START_ANGLE} 120 120)`}
        />
        {isLight && (
          <circle className="ring-knob" cx={KNOB_X} cy={KNOB_Y} r="8.2" />
        )}
      </svg>

      {isLight ? (
        <MusicNote className="ring-icon ring-note" />
      ) : (
        <>
          <Zzz />
          <Moon className="ring-icon ring-moon" />
        </>
      )}

      <button
        className="time-display"
        type="button"
        onClick={onOpenTime}
        aria-label={`Alarm time ${time.hour}:${time.minute} ${time.period}. Tap to change.`}
      >
        <span className="time-digits">
          {time.hour}:{time.minute}
        </span>
        <span className="time-period">{time.period}</span>
      </button>
    </div>
  )
}

function DayPicker({ days, onToggle, pulse }) {
  return (
    <div className={`day-picker${pulse ? ' day-picker-pulse' : ''}`}>
      {DAY_LABELS.map((label, index) => {
        const selected = days.includes(index)
        return (
          <button
            className={`day${selected ? ' day-selected' : ''}`}
            type="button"
            key={`${label}-${index}`}
            aria-pressed={selected}
            aria-label={`Toggle ${DAY_NAMES[index]}`}
            onClick={() => onToggle(index)}
          >
            {label}
          </button>
        )
      })}
    </div>
  )
}

function Toggle({ checked, onChange, label }) {
  return (
    <button
      className={`toggle${checked ? ' toggle-on' : ''}`}
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={label}
      onClick={() => onChange(!checked)}
    >
      <span className="toggle-thumb" />
    </button>
  )
}

function TimeSheet({ alarm, onClose, onSetTime }) {
  const [value, setValue] = useState(() => toPickerTime(alarm.time))

  const nudgeHour = (amount) => {
    setValue((current) => ({
      ...current,
      hour: ((current.hour - 1 + amount + 12) % 12) + 1,
    }))
  }

  const nudgeMinute = (amount) => {
    setValue((current) => ({
      ...current,
      minute: (current.minute + amount + 60) % 60,
    }))
  }

  return (
    <div className="sheet-backdrop" onMouseDown={onClose}>
      <div
        className="sheet time-sheet"
        role="dialog"
        aria-modal="true"
        aria-label="Set alarm time"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="sheet-grabber" />
        <div className="sheet-header">
          <h2>Set alarm</h2>
          <button className="sheet-text-button" type="button" onClick={onClose}>
            Cancel
          </button>
        </div>

        <div className="time-editor">
          <div className="time-stepper">
            <button
              className="stepper-button"
              type="button"
              aria-label="Increase hour"
              onClick={() => nudgeHour(1)}
            >
              <ChevronUp />
            </button>
            <span className="stepper-value">{String(value.hour).padStart(2, '0')}</span>
            <button
              className="stepper-button"
              type="button"
              aria-label="Decrease hour"
              onClick={() => nudgeHour(-1)}
            >
              <ChevronDown />
            </button>
          </div>

          <span className="time-colon">:</span>

          <div className="time-stepper">
            <button
              className="stepper-button"
              type="button"
              aria-label="Increase minutes"
              onClick={() => nudgeMinute(5)}
            >
              <ChevronUp />
            </button>
            <span className="stepper-value">{String(value.minute).padStart(2, '0')}</span>
            <button
              className="stepper-button"
              type="button"
              aria-label="Decrease minutes"
              onClick={() => nudgeMinute(-5)}
            >
              <ChevronDown />
            </button>
          </div>

          <div className="period-switch" role="group" aria-label="AM or PM">
            {['AM', 'PM'].map((period) => (
              <button
                className={value.period === period ? 'period-active' : ''}
                type="button"
                key={period}
                aria-pressed={value.period === period}
                onClick={() => setValue((current) => ({ ...current, period }))}
              >
                {period}
              </button>
            ))}
          </div>
        </div>

        <p className="sheet-hint">Minutes move in five minute steps.</p>

        <button
          className="sheet-primary"
          type="button"
          onClick={() => onSetTime(fromPickerTime(value))}
        >
          Set time
        </button>
      </div>
    </div>
  )
}

function SoundSheet({ alarm, onClose, onChange }) {
  return (
    <div className="sheet-backdrop" onMouseDown={onClose}>
      <div
        className="sheet sound-sheet"
        role="dialog"
        aria-modal="true"
        aria-label="Choose alarm sound"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="sheet-grabber" />
        <div className="sheet-header">
          <h2>Sounds</h2>
          <button className="sheet-text-button" type="button" onClick={onClose}>
            Done
          </button>
        </div>

        <div className="sound-list">
          {SOUNDS.map((sound) => {
            const selected = sound.name === alarm.sound
            return (
              <button
                className={`sound-row${selected ? ' sound-row-selected' : ''}`}
                type="button"
                key={sound.name}
                onClick={() => {
                  onChange(sound.name)
                  previewSound(sound.name)
                }}
              >
                <span className="sound-copy">
                  <span className="sound-name">{sound.name}</span>
                  <span className="sound-description">{sound.description}</span>
                </span>
                {selected && (
                  <span className="sound-check">
                    <CheckIcon />
                  </span>
                )}
              </button>
            )
          })}
        </div>
      </div>
    </div>
  )
}

export default function Phone({ alarm, onChange, onSave, onReset, toast }) {
  const [sheet, setSheet] = useState(null)
  const [repeatPulse, setRepeatPulse] = useState(false)
  const pulseTimer = useRef(null)

  useEffect(() => {
    return () => window.clearTimeout(pulseTimer.current)
  }, [])

  const update = (changes) => onChange(alarm.id, changes)

  const pulseRepeat = () => {
    setRepeatPulse(true)
    window.clearTimeout(pulseTimer.current)
    pulseTimer.current = window.setTimeout(() => setRepeatPulse(false), 750)
  }

  const toggleDay = (day) => {
    const days = alarm.days.includes(day)
      ? alarm.days.filter((selectedDay) => selectedDay !== day)
      : [...alarm.days, day].sort((a, b) => a - b)
    update({ days })
  }

  return (
    <article className={`phone phone-${alarm.theme}`}>
      <div className="phone-inner">
        <StatusBar />

        <div className="screen-body">
          <h1 className="screen-title">Settings</h1>

          <ClockRing alarm={alarm} onOpenTime={() => setSheet('time')} />

          <DayPicker days={alarm.days} onToggle={toggleDay} pulse={repeatPulse} />

          <div className="settings-list">
            <button
              className="setting-row"
              type="button"
              onClick={() => setSheet('sound')}
            >
              <span className="setting-label">Sounds</span>
              <span className="setting-value">
                {alarm.sound}
                <ChevronRight />
              </span>
            </button>

            <button className="setting-row" type="button" onClick={pulseRepeat}>
              <span className="setting-label">Repeat</span>
              <span className="setting-value">
                {formatDays(alarm.days)}
                <ChevronRight />
              </span>
            </button>

            <div className="setting-row">
              <span className="setting-label">Snooze</span>
              <Toggle
                checked={alarm.snooze}
                onChange={(snooze) => update({ snooze })}
                label="Snooze"
              />
            </div>
          </div>

          <div className="phone-actions">
            <button
              className="reset-button"
              type="button"
              onClick={() => onReset(alarm.id)}
            >
              Reset
            </button>
            <button
              className="save-button"
              type="button"
              onClick={() => onSave(alarm.id)}
            >
              Save
            </button>
          </div>
        </div>

        <div className="home-indicator" />

        {toast && (
          <div className="phone-toast" role="status" key={toast}>
            {toast}
          </div>
        )}

        {sheet === 'time' && (
          <TimeSheet
            alarm={alarm}
            onClose={() => setSheet(null)}
            onSetTime={(time) => {
              update({ time })
              setSheet(null)
            }}
          />
        )}

        {sheet === 'sound' && (
          <SoundSheet
            alarm={alarm}
            onClose={() => setSheet(null)}
            onChange={(sound) => update({ sound })}
          />
        )}
      </div>
    </article>
  )
}
