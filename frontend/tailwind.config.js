/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // CaixaCerto brand colors (from original CSS)
        primary: {
          50: '#E3F3EC',
          100: '#C5E7D8',
          200: '#8BCDB0',
          300: '#5FD3A5',
          400: '#3DBD8D',
          500: '#12805C',
          600: '#0C5E43',
          700: '#094331',
          800: '#072E21',
          900: '#051D15',
        },
        dark: {
          50: '#E7EEEC',
          100: '#DCE4E1',
          200: '#B8C8C4',
          300: '#8FAFA7',
          400: '#6B9089',
          500: '#52636B',
          600: '#424E54',
          700: '#333C40',
          800: '#242A2E',
          900: '#14263B',
        },
        accent: {
          red: '#B8402C',
          redLight: '#FFB4A3',
          green: '#12805C',
          greenLight: '#5FD3A5',
        }
      },
      fontFamily: {
        sans: ['DM Sans', 'system-ui', '-apple-system', 'Segoe UI', 'Arial', 'sans-serif'],
        heading: ['Sora', 'DM Sans', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        'xl': '16px',
        '2xl': '20px',
      },
      boxShadow: {
        'card': '0 2px 8px rgba(20, 38, 59, 0.08)',
        'card-hover': '0 4px 16px rgba(20, 38, 59, 0.12)',
      },
    },
  },
  plugins: [],
}