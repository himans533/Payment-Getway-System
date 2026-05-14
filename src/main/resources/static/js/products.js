(function () {
    const searchInput = document.getElementById("productSearch");
    const productCards = document.querySelectorAll(".product-card");
    const filterButtons = document.querySelectorAll(".filter-btn");

    function applyFilters() {
        const searchValue = searchInput ? searchInput.value.toLowerCase() : "";
        const activeFilter = document.querySelector(".filter-btn.active");
        const category = activeFilter ? activeFilter.dataset.category : "all";

        productCards.forEach(function (card) {
            const matchesName = card.dataset.name.toLowerCase().includes(searchValue);
            const matchesCategory = category === "all" || card.dataset.category === category;

            card.style.display = matchesName && matchesCategory ? "" : "none";
        });
    }

    if (searchInput) {
        searchInput.addEventListener("input", applyFilters);
    }

    filterButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            filterButtons.forEach(function (item) {
                item.classList.remove("active");
            });
            button.classList.add("active");
            applyFilters();
        });
    });
})();
