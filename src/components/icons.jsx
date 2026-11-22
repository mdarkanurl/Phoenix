export function ChevronRight({ size = 14 }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="m9 18 6-6-6-6" />
    </svg>
  )
}

export function ChevronUp({ size = 16 }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="m18 15-6-6-6 6" />
    </svg>
  )
}

export function ChevronDown({ size = 16 }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="m6 9 6 6 6-6" />
    </svg>
  )
}

export function CheckIcon({ size = 16 }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.4"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="m20 6-11 11-5-5" />
    </svg>
  )
}

export function MusicNote({ className = '' }) {
  return (
    <svg
      className={className}
      width="27"
      height="27"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M9 18V5.5l9-2v11.7"
        stroke="currentColor"
        strokeWidth="2.1"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <ellipse cx="6.7" cy="18.2" rx="2.6" ry="2.1" fill="currentColor" />
      <ellipse cx="15.7" cy="15.4" rx="2.6" ry="2.1" fill="currentColor" />
    </svg>
  )
}

export function Moon({ className = '' }) {
  return (
    <svg
      className={className}
      width="28"
      height="28"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M20.1 14.4A8.2 8.2 0 0 1 9.6 3.9 8.3 8.3 0 1 0 20.1 14.4Z"
        fill="currentColor"
      />
    </svg>
  )
}

export function Zzz() {
  return (
    <span className="zzz" aria-hidden="true">
      z<sup>z</sup>
    </span>
  )
}

export function StatusBar() {
  return (
    <div className="status-bar" aria-hidden="true">
      <span className="status-time">9:41</span>
      <div className="status-icons">
        <svg width="17" height="12" viewBox="0 0 17 12" fill="currentColor">
          <rect x="0" y="7" width="3" height="5" rx="1" />
          <rect x="4.6" y="5" width="3" height="7" rx="1" />
          <rect x="9.2" y="2.6" width="3" height="9.4" rx="1" />
          <rect x="13.8" y="0" width="3" height="12" rx="1" />
        </svg>
        <svg width="16" height="12" viewBox="0 0 16 12" fill="none">
          <path
            d="M1.1 4.2a10.5 10.5 0 0 1 13.8 0M3.5 6.7a7 7 0 0 1 9 0M6 9.2a3.4 3.4 0 0 1 4 0"
            stroke="currentColor"
            strokeWidth="1.6"
            strokeLinecap="round"
          />
          <circle cx="8" cy="11" r=".9" fill="currentColor" />
        </svg>
        <svg width="25" height="12" viewBox="0 0 25 12" fill="none">
          <rect
            x=".7"
            y=".7"
            width="20.6"
            height="10.6"
            rx="3"
            stroke="currentColor"
            strokeOpacity=".45"
            strokeWidth="1.2"
          />
          <rect x="2.4" y="2.4" width="17.2" height="7.2" rx="1.8" fill="currentColor" />
          <path
            d="M23 4.2v3.6c.9-.3 1.4-.9 1.4-1.8s-.5-1.5-1.4-1.8Z"
            fill="currentColor"
            fillOpacity=".5"
          />
        </svg>
      </div>
    </div>
  )
}


