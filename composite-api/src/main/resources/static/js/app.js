const form = document.querySelector("#product-search-form");
const loginForm = document.querySelector("#login-form");
const loginPanel = document.querySelector("#login-panel");
const searchPanel = document.querySelector("#search-panel");
const logoutButton = document.querySelector("#logout-button");
const productIdInput = document.querySelector("#product-id");
const searchButton = document.querySelector("#search-button");
const requestMessage = document.querySelector("#request-message");
const productResult = document.querySelector("#product-result");
let accessToken;

const fields = {
    category: document.querySelector("#product-category"),
    title: document.querySelector("#product-title"),
    description: document.querySelector("#product-description"),
    stock: document.querySelector("#stock-badge"),
    priceMxn: document.querySelector("#price-mxn"),
    priceUsd: document.querySelector("#price-usd"),
    currencySource: document.querySelector("#currency-source"),
    availableUnits: document.querySelector("#available-units"),
    warehouseLocation: document.querySelector("#warehouse-location"),
    deliveryDays: document.querySelector("#delivery-days"),
    productId: document.querySelector("#product-id-result")
};

const currencyFormatters = {
    MXN: new Intl.NumberFormat("es-MX", {
        style: "currency",
        currency: "MXN"
    }),
    USD: new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD"
    })
};

function showMessage(message, state = "error") {
    requestMessage.textContent = message;
    requestMessage.dataset.state = state;
    requestMessage.hidden = false;
}

function renderSummary(summary) {
    fields.category.textContent = summary.category;
    fields.title.textContent = summary.title;
    fields.description.textContent = summary.description;
    fields.stock.textContent = summary.stock_status === "IN_STOCK"
        ? "Disponible"
        : "Agotado";
    fields.stock.dataset.stock = summary.stock_status;
    fields.priceMxn.textContent = currencyFormatters.MXN.format(summary.price_mxn);
    fields.priceUsd.textContent = currencyFormatters.USD.format(summary.price_usd);
    fields.currencySource.textContent = `Conversión: ${summary.currency_conversion_source}`;
    fields.availableUnits.textContent = summary.available_units.toLocaleString("es-MX");
    fields.warehouseLocation.textContent = summary.warehouse_location;
    fields.deliveryDays.textContent = summary.estimated_delivery_days.toLocaleString("es-MX");
    fields.productId.textContent = `ID del producto: ${summary.product_id}`;
    productResult.hidden = false;
}

async function renewAccessToken() {
    const response = await fetch("/auth/refresh", { method: "POST" });
    if (response.status === 401) {
        accessToken = undefined;
        return false;
    }
    if (!response.ok) {
        throw new Error("No fue posible renovar la sesión.");
    }
    accessToken = (await response.json()).accessToken;
    return true;
}

async function fetchSummary(url) {
    const sendRequest = () => fetch(url, {
        headers: {
            Accept: "application/json",
            Authorization: `Bearer ${accessToken}`
        }
    });
    let response = await sendRequest();
    if (response.status === 401 && await renewAccessToken()) {
        response = await sendRequest();
    }
    return response;
}

function showLogin() {
    loginPanel.hidden = false;
    searchPanel.hidden = true;
}

function showSearch() {
    loginPanel.hidden = true;
    searchPanel.hidden = false;
}

loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(loginForm);
    try {
        const response = await fetch("/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                username: formData.get("username"),
                password: formData.get("password")
            })
        });
        if (!response.ok) {
            throw new Error(response.status === 401
                ? "Usuario o contraseña incorrectos."
                : "No fue posible iniciar sesión.");
        }
        accessToken = (await response.json()).accessToken;
        loginForm.reset();
        showSearch();
        form.requestSubmit();
    } catch (error) {
        showMessage(error instanceof Error ? error.message : "No fue posible iniciar sesión.");
    }
});

logoutButton.addEventListener("click", async () => {
    try {
        const response = await fetch("/auth/logout", { method: "POST" });
        if (!response.ok) {
            throw new Error("No fue posible cerrar la sesión en el servidor.");
        }
    } catch (error) {
        showMessage(error instanceof Error ? error.message : "No fue posible cerrar sesión.");
    } finally {
        accessToken = undefined;
        productResult.hidden = true;
        showLogin();
    }
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const productId = productIdInput.value.trim();
    if (!productId) {
        productIdInput.focus();
        return;
    }

    searchButton.disabled = true;
    requestMessage.hidden = false;
    showMessage("Consultando producto, inventario y tipo de cambio...", "loading");
    productResult.hidden = true;

    try {
        const response = await fetchSummary(
            `/api/v1/products/${encodeURIComponent(productId)}/summary`
        );
        if (response.status === 401) {
            showLogin();
            throw new Error("La sesión expiró. Inicia sesión de nuevo.");
        }

        if (!response.ok) {
            if (response.status === 404) {
                throw new Error("No se encontró el producto o su inventario.");
            }
            if (response.status === 502) {
                throw new Error("Uno de los servicios no está disponible. Intenta de nuevo.");
            }
            throw new Error("No fue posible consultar el producto. Intenta de nuevo.");
        }

        renderSummary(await response.json());
        requestMessage.hidden = true;
    } catch (error) {
        showMessage(error instanceof Error
            ? error.message
            : "Ocurrió un error inesperado al consultar el producto.");
    } finally {
        searchButton.disabled = false;
    }
});

(async () => {
    try {
        if (await renewAccessToken()) {
            showSearch();
            form.requestSubmit();
        } else {
            showLogin();
        }
    } catch (error) {
        showLogin();
        showMessage(error instanceof Error
            ? error.message
            : "No fue posible validar la sesión.");
    }
})();
