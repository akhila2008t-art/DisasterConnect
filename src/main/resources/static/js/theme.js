/* =========================================================
   DISASTERCONNECT — GLOBAL THEME CONTROLLER
   Light / Dark Mode
   ========================================================= */

(function () {
    "use strict";

    const STORAGE_KEY = "disasterconnect-theme";

    const THEMES = {
        LIGHT: "light",
        DARK: "dark"
    };

    /**
     * Get the user's preferred theme.
     */
    function getPreferredTheme() {
        const savedTheme = localStorage.getItem(STORAGE_KEY);

        if (
            savedTheme === THEMES.LIGHT ||
            savedTheme === THEMES.DARK
        ) {
            return savedTheme;
        }

        /*
         * If the user has never selected a theme,
         * follow the operating system preference.
         */
        if (
            window.matchMedia &&
            window.matchMedia(
                "(prefers-color-scheme: dark)"
            ).matches
        ) {
            return THEMES.DARK;
        }

        return THEMES.LIGHT;
    }


    /**
     * Apply the selected theme to the document.
     */
    function applyTheme(theme) {
        const safeTheme =
            theme === THEMES.DARK
                ? THEMES.DARK
                : THEMES.LIGHT;

        document.documentElement.setAttribute(
            "data-theme",
            safeTheme
        );

        localStorage.setItem(
            STORAGE_KEY,
            safeTheme
        );

        updateThemeControls(safeTheme);
    }


    /**
     * Switch between light and dark mode.
     */
    function toggleTheme() {
        const currentTheme =
            document.documentElement.getAttribute(
                "data-theme"
            ) || getPreferredTheme();

        const nextTheme =
            currentTheme === THEMES.DARK
                ? THEMES.LIGHT
                : THEMES.DARK;

        applyTheme(nextTheme);
    }


    /**
     * Update theme buttons/icons wherever they exist.
     */
    function updateThemeControls(theme) {
        const isDark = theme === THEMES.DARK;

        document
            .querySelectorAll("[data-theme-toggle]")
            .forEach((button) => {

                button.setAttribute(
                    "aria-label",
                    isDark
                        ? "Switch to light mode"
                        : "Switch to dark mode"
                );

                button.setAttribute(
                    "title",
                    isDark
                        ? "Switch to light mode"
                        : "Switch to dark mode"
                );

                const icon =
                    button.querySelector(
                        "[data-theme-icon]"
                    );

                if (icon) {
                    icon.textContent =
                        isDark ? "☀" : "☾";
                }

                const label =
                    button.querySelector(
                        "[data-theme-label]"
                    );

                if (label) {
                    label.textContent =
                        isDark
                            ? "Light mode"
                            : "Dark mode";
                }
            });


        /*
         * Optional theme-select controls.
         */
        document
            .querySelectorAll("[data-theme-select]")
            .forEach((select) => {
                select.value = theme;
            });
    }


    /**
     * Allow pages to directly select a theme.
     */
    function setTheme(theme) {
        applyTheme(theme);
    }


    /**
     * Initialize theme as early as possible.
     */
    function initializeTheme() {
        const theme = getPreferredTheme();

        document.documentElement.setAttribute(
            "data-theme",
            theme
        );

        /*
         * Wait until DOM controls exist before
         * attempting to update them.
         */
        if (document.readyState === "loading") {
            document.addEventListener(
                "DOMContentLoaded",
                function () {
                    updateThemeControls(theme);
                }
            );
        } else {
            updateThemeControls(theme);
        }
    }


    /*
     * Initialize immediately.
     *
     * This helps prevent the page from briefly
     * showing the wrong theme during loading.
     */
    initializeTheme();


    /*
     * Global click handler.
     *
     * Any button with:
     *
     * data-theme-toggle
     *
     * will automatically toggle the theme.
     */
    document.addEventListener(
        "click",
        function (event) {

            const toggle =
                event.target.closest(
                    "[data-theme-toggle]"
                );

            if (!toggle) {
                return;
            }

            toggleTheme();
        }
    );


    /*
     * Theme dropdown support.
     */
    document.addEventListener(
        "change",
        function (event) {

            const select =
                event.target.closest(
                    "[data-theme-select]"
                );

            if (!select) {
                return;
            }

            setTheme(select.value);
        }
    );


    /*
     * Listen for operating-system theme changes.
     *
     * We only respond automatically when the user
     * has not manually selected a theme.
     */
    if (window.matchMedia) {

        const mediaQuery =
            window.matchMedia(
                "(prefers-color-scheme: dark)"
            );

        const handleSystemThemeChange =
            function (event) {

                const savedTheme =
                    localStorage.getItem(
                        STORAGE_KEY
                    );

                if (savedTheme) {
                    return;
                }

                applyTheme(
                    event.matches
                        ? THEMES.DARK
                        : THEMES.LIGHT
                );
            };

        if (mediaQuery.addEventListener) {
            mediaQuery.addEventListener(
                "change",
                handleSystemThemeChange
            );
        }
    }


    /*
     * Expose a small public API.
     *
     * This lets individual pages use:
     *
     * DisasterConnectTheme.toggle()
     * DisasterConnectTheme.set("dark")
     * DisasterConnectTheme.set("light")
     */
    window.DisasterConnectTheme = {
        toggle: toggleTheme,
        set: setTheme,
        get: function () {
            return (
                document.documentElement.getAttribute(
                    "data-theme"
                ) || getPreferredTheme()
            );
        }
    };

})();