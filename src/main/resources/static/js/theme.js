document.addEventListener("DOMContentLoaded", () => {
    const themeToggle = document.getElementById("theme-toggle");
    const themeIcon = document.getElementById("theme-icon");

    const savedTheme = localStorage.getItem("theme");

    const preferredTheme =
        window.matchMedia("(prefers-color-scheme: dark)").matches
            ? "dark"
            : "light";

    const initialTheme = savedTheme ?? preferredTheme;

    applyTheme(initialTheme);

    themeToggle?.addEventListener("click", () => {
        const currentTheme =
            document.documentElement.getAttribute("data-theme");

        const newTheme = currentTheme === "dark"
            ? "light"
            : "dark";

        applyTheme(newTheme);
        localStorage.setItem("theme", newTheme);
    });

    function applyTheme(theme) {
        document.documentElement.setAttribute("data-theme", theme);

        if (!themeIcon) {
            return;
        }

        themeIcon.className =
            theme === "dark"
                ? "bi bi-sun-fill"
                : "bi bi-moon-fill";
    }
});