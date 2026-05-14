(function () {
    const forms = document.querySelectorAll(".auth-form");
    const toggles = document.querySelectorAll("[data-toggle-password]");

    toggles.forEach((toggle) => {
        toggle.addEventListener("click", () => {
            const field = toggle.closest(".password-field");
            const input = field ? field.querySelector("input") : null;

            if (!input) {
                return;
            }

            const isHidden = input.type === "password";
            input.type = isHidden ? "text" : "password";
            toggle.textContent = isHidden ? "Hide" : "Show";
            toggle.setAttribute("aria-label", isHidden ? "Hide password" : "Show password");
        });
    });

    forms.forEach((form) => {
        const fields = form.querySelectorAll(".field");

        fields.forEach((field) => {
            const input = field.querySelector("input");

            if (!input) {
                return;
            }

            input.addEventListener("blur", () => markField(field, input));
            input.addEventListener("input", () => {
                if (field.classList.contains("is-invalid")) {
                    markField(field, input);
                }
            });
        });

        form.addEventListener("submit", (event) => {
            let hasError = false;

            fields.forEach((field) => {
                const input = field.querySelector("input");

                if (!input) {
                    return;
                }

                markField(field, input);

                if (!input.checkValidity()) {
                    hasError = true;
                }
            });

            if (hasError) {
                event.preventDefault();
            }
        });
    });

    function markField(field, input) {
        field.classList.toggle("is-invalid", !input.checkValidity());
    }
})();
