const { fontFamily } = require("tailwindcss/defaultTheme");

module.exports = {
  content: ["./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    container: {
      center: true,
      padding: "2rem",
    },
    screens: {
      "2xl": "1536px",
      xl: { max: "1536px" },
      lg: { max: "1280px" },
      md: { max: "768px" },
      sm: { max: "640px" },
      xs: { max: "360px" },
    },
    extend: {
      colors: {
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        checked: "hsl(var(--checked))",
        disabled: "hsl(var(--disabled))",
        alert:"hsl(var(--alert))",
        pin: {
          DEFAULT: "hsl(var(--pin))",
          focus: "hsl(var(--pin-focus))",
        },
        primary: {
          DEFAULT: "hsl(var(--primary))",
          foreground: "hsl(var(--primary-foreground))",
        },
        secondary: {
          DEFAULT: "hsl(var(--secondary))",
          foreground: "hsl(var(--secondary-foreground))",
        },
        destructive: {
          DEFAULT: "hsl(var(--destructive))",
          foreground: "hsl(var(--destructive-foreground))",
        },
        muted: {
          DEFAULT: "hsl(var(--muted))",
          foreground: "hsl(var(--muted-foreground))",
          "neutral-gray": "hsl(var(--muted-neutral-gray))",
          "dark-gray": "hsl(var(--muted-dark-gray))",
          "light-gray": "hsl(var(--muted-light-gray))",
        },
        accent: {
          DEFAULT: "hsl(var(--accent))",
          foreground: "hsl(var(--accent-foreground))",
        },
        popover: {
          DEFAULT: "hsl(var(--popover))",
          foreground: "hsl(var(--popover-foreground))",
        },
        card: {
          DEFAULT: "hsl(var(--card))",
          foreground: "hsl(var(--card-foreground))",
        },
      },
      borderRadius: {
        "2xl": "calc(var(--radius) + 12px)",
        xl: "calc(var(--radius) + 2px)",
        lg: `var(--radius)`,
        md: `calc(var(--radius) - 2px)`,
        sm: "calc(var(--radius) - 4px)",
      },
      boxShadow: {
        md: "0px 2px 5px rgba(0, 0, 0, 0.10)",
        lg: "0px 4px 10px rgba(0, 0, 0, 0.10)",
      },
      fontFamily: {
        inter: ["var(--font-inter)", ...fontFamily.sans],
        kantumruypro: ["var(--font-kantumruypro)", ...fontFamily.sans],
      },
      keyframes: {
        "accordion-down": {
          from: { height: "0" },
          to: { height: "var(--radix-accordion-content-height)" },
        },
        "accordion-up": {
          from: { height: "var(--radix-accordion-content-height)" },
          to: { height: "0" },
        },
      },
      animation: {
        "accordion-down": "accordion-down 0.2s ease-out",
        "accordion-up": "accordion-up 0.2s ease-out",
      },
    },
  },
  plugins: [
    require("tailwindcss-animate"),
    require("@tailwindcss/line-clamp"),
    require("tailwindcss-dir")(),
  ],
};
