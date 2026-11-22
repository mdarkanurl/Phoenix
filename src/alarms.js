export const DAY_LABELS = ['S', 'M', 'T', 'W', 'T', 'F', 'S']
export const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

export const SOUNDS = [
  { name: 'Slow Rise', description: 'A gentle, sunlit chime' },
  { name: 'Night Owl', description: 'Soft bells in the dark' },
  { name: 'Morning Dew', description: 'Bright and quietly hopeful' },
  { name: 'Soft Chime', description: 'A simple, warm reminder' },
]

export const DEFAULT_ALARMS = [
  {
    id: 'morning',
    name: 'Morning alarm',
    theme: 'light',
    time: '07:00',
    days: [1, 3, 5],
    sound: 'Slow Rise',
    snooze: true,
  },
  {
    id: 'night',
    name: 'Night alarm',
    theme: 'dark',
    time: '23:00',
    days: [0, 2, 3, 4],
    sound: 'Night Owl',
    snooze: true,
  },
]

export function cloneDefaults() {
  return DEFAULT_ALARMS.map((alarm) => ({ ...alarm, days: [...alarm.days] }))
}

export function formatTime(time) {
  const [hours, minutes] = time.split(':').map(Number)
  const period = hours >= 12 ? 'PM' : 'AM'
  const hour = hours % 12 || 12
  return {
    hour: String(hour),
    minute: String(minutes).padStart(2, '0'),
    period,
  }
}

export function toPickerTime(time) {
  const [hours, minutes] = time.split(':').map(Number)
  return {
    hour: hours % 12 || 12,
    minute: minutes,
    period: hours >= 12 ? 'PM' : 'AM',
  }
}

export function fromPickerTime({ hour, minute, period }) {
  let hours = hour % 12
  if (period === 'PM') hours += 12
  return `${String(hours).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
}

export function formatDays(days) {
  if (days.length === 0) return 'Never'
  if (days.length === 7) return 'Every day'
  if (days.length === 5 && [1, 2, 3, 4, 5].every((day) => days.includes(day))) {
    return 'Weekdays'
  }
  if (days.length === 2 && days.includes(0) && days.includes(6)) return 'Weekends'
  return [...days].sort((a, b) => a - b).map((day) => DAY_NAMES[day]).join(' ')
}
