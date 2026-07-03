/** @type {import('tailwindcss').Config} */

// Brand palette, centralized. The app was built on Tailwind's `slate` (neutrals) and
// `indigo`/`teal` (accents). Instead of touching every component, we remap those three
// scales here to the client's identity — warm near-black, very light beige, and yellow —
// keeping each scale's light→dark ordering so nothing inverts. Change a value here and the
// whole UI re-colors. Status colors (amber/emerald/rose) keep Tailwind's defaults on purpose.

// Neutrals: warm "sand" — light end is very light beige (text), dark end warm near-black (bg).
const sand = {
  50: '#faf6ec',
  100: '#f3ecdd',
  200: '#e7dcc5',
  300: '#d3c4a6',
  400: '#a99b80',
  500: '#857a63',
  600: '#5e5648',
  700: '#3d382e',
  800: '#2a261f',
  900: '#1b1813',
  950: '#12100b',
}

// Primary accent: deep gold (the darker stop of gradients and primary highlights).
const gold = {
  50: '#fdf6e3',
  100: '#faedc2',
  200: '#f5db85',
  300: '#f2c94c',
  400: '#ebb714',
  500: '#d9a400',
  600: '#b08400',
  700: '#875f00',
  800: '#5f4300',
  900: '#3d2b00',
  950: '#241900',
}

// Secondary accent: bright yellow (the lighter gradient stop, focus rings, chips).
const yellow = {
  50: '#fffaeb',
  100: '#fef2c7',
  200: '#fce9a6',
  300: '#fbde7e',
  400: '#f7ce45',
  500: '#efc01f',
  600: '#d1a30a',
  700: '#a67d0c',
  800: '#7c5e10',
  900: '#5c4611',
  950: '#352805',
}

export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      colors: {
        slate: sand,
        indigo: gold,
        teal: yellow,
      },
      // "New message" indicator: a customer's name pulses green in the agent's list.
      // We animate the background colour (not opacity), so the chip never turns translucent
      // and never looks like it's overlapping the neighbouring chip.
      keyframes: {
        blink: {
          '0%, 100%': { backgroundColor: 'rgba(16, 185, 129, 0.30)' },
          '50%': { backgroundColor: 'rgba(16, 185, 129, 0.06)' },
        },
      },
      animation: {
        blink: 'blink 1s ease-in-out infinite',
      },
    },
  },
  plugins: [],
}
