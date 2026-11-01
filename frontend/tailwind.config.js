/** @type {import('tailwindcss').Config} */
// Token màu/chữ lấy từ design/html/app.css (bản HTML đã QA).
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#C74615', // --orange
          dark: '#A7370F',    // hover nút chính
          light: '#FFF2E9',   // --soft: nền cam nhạt
          soft: '#FFF2E9',
          accent: '#F47637',  // --accent
        },
        ink: {
          DEFAULT: '#202126', // --ink
          lighter: '#62656D', // --muted
        },
        surface: {
          DEFAULT: '#FFFFFF',
          gray: '#F6F7F9',    // --canvas
          border: '#E7E8EB',  // --line
        },
        success: { DEFAULT: '#157450', soft: '#E9F6EF' },
        danger: { DEFAULT: '#B63838', soft: '#FFF0F0' },
        info: { DEFAULT: '#2E61AD', soft: '#EDF3FC' },
      },
      fontFamily: {
        sans: ['"Be Vietnam Pro"', 'Arial', 'sans-serif'],
      },
      boxShadow: {
        card: '0 12px 28px #2021260c',
        primary: '0 4px 14px #c7461520',
      },
    },
  },
  plugins: [],
}
