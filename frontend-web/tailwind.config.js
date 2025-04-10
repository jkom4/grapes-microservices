/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'primary': '#FFD0F2',
        'secondary': '#9E14D0',
        'accent': '#EB50AD',
      },
    },
  },
  plugins: [],
}
