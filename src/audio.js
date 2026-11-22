let audioContext

export function ensureAudio() {
  if (typeof window === 'undefined') return null
  const AudioContext = window.AudioContext || window.webkitAudioContext
  if (!AudioContext) return null
  if (!audioContext) audioContext = new AudioContext()
  if (audioContext.state === 'suspended') audioContext.resume()
  return audioContext
}

const SOUND_PATTERNS = {
  'Slow Rise': [
    [523.25, 0, 0.8],
    [659.25, 0.32, 0.9],
    [783.99, 0.68, 1.1],
    [1046.5, 1.12, 1.5],
  ],
  'Night Owl': [
    [392, 0, 1.1],
    [523.25, 0.46, 1],
    [440, 0.94, 1.2],
    [349.23, 1.5, 1.6],
  ],
  'Morning Dew': [
    [659.25, 0, 0.6],
    [880, 0.22, 0.7],
    [987.77, 0.5, 0.9],
    [1318.51, 0.88, 1.2],
  ],
  'Soft Chime': [
    [440, 0, 0.9],
    [554.37, 0.28, 1],
    [659.25, 0.6, 1.4],
  ],
}

function scheduleSound(name, volume = 0.12, octave = 1) {
  const context = ensureAudio()
  if (!context) return
  const pattern = SOUND_PATTERNS[name] || SOUND_PATTERNS['Soft Chime']
  const now = context.currentTime + 0.03

  pattern.forEach(([frequency, delay, duration]) => {
    const oscillator = context.createOscillator()
    const gain = context.createGain()

    oscillator.type = 'sine'
    oscillator.frequency.value = frequency * octave
    gain.gain.setValueAtTime(0.0001, now + delay)
    gain.gain.exponentialRampToValueAtTime(volume, now + delay + 0.04)
    gain.gain.exponentialRampToValueAtTime(0.0001, now + delay + duration)

    oscillator.connect(gain)
    gain.connect(context.destination)
    oscillator.start(now + delay)
    oscillator.stop(now + delay + duration + 0.05)
  })
}

export function previewSound(name) {
  scheduleSound(name, 0.09)
}

export function playAlarm(name) {
  scheduleSound(name, 0.16)
  window.setTimeout(() => scheduleSound(name, 0.12, 1.5), 850)
}
