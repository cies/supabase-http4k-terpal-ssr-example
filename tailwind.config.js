// tailwind.config.js
module.exports = {
    content: [], // You can leave this empty or include actual files
    safelist: [
        {
            pattern: /.*/, // Match everything
        },
    ],
    theme: {
        extend: {},
    },
    plugins: [
        require('@tailwindcss/forms')({
            strategy: 'base', // or 'class'
        }),
    ],
}
