/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#C74615', // Cam chủ đạo Figma
          light: '#dec4b1',   // Nền cam nhạt (Utility bar, Hero)
        },
        ink: {
          DEFAULT: '#202126', // Text đen đậm
          lighter: '#62656D', // Text xám nhạt
        },
        surface: {
          DEFAULT: '#FFFFFF',
          gray: '#F7F8FA',    // Nền xám (Search, Footer)
          border: '#E7E8EB'   // Viền
        }
      },
      fontFamily: {
        sans: ['"Be Vietnam Pro"', 'sans-serif'], // Font chuẩn Figma
      }
    },
  },
  plugins: [],
}

