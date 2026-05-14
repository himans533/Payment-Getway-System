(function () {
    const body = document.body;
    const menuToggle = document.querySelector("[data-menu-toggle]");
    const menuOverlay = document.querySelector("[data-menu-overlay]");
    const navLinks = document.querySelectorAll(".nav-list a");

    function closeMenu() {
        body.classList.remove("menu-open");
        if (menuToggle) {
            menuToggle.setAttribute("aria-expanded", "false");
        }
    }

    if (menuToggle) {
        menuToggle.addEventListener("click", function () {
            const isOpen = body.classList.toggle("menu-open");
            menuToggle.setAttribute("aria-expanded", String(isOpen));
        });
    }

    if (menuOverlay) {
        menuOverlay.addEventListener("click", closeMenu);
    }

    navLinks.forEach(function (link) {
        link.addEventListener("click", closeMenu);
    });

    window.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            closeMenu();
        }
    });
})();
