const navToggleButton = document.getElementById("nav-toggle-btn");
const staffSignInForm =
  document.getElementById("staff-signin-form") ??
  document.querySelector(".staff-signin-form");
const signInStatus = document.getElementById("signin-status");
const adminCreateForm = document.getElementById("admin-create-form");
const adminStatus = document.getElementById("admin-status");
const adminListings = document.getElementById("admin-listings");
const signOutLink = document.getElementById("staff-signout-link");
const browseFiltersForm = document.getElementById("browseFilters");
const itemGrid = document.getElementById("itemGrid");
const browseStatus = document.getElementById("browse-status");
const browseResultsMeta = document.getElementById("browse-results-meta");

let browseItemsState = [];

navToggleButton?.addEventListener("click", () => {
  document.body.classList.toggle("nav-open");
});

function updateStatusBanner(element, message, variant) {
  if (!element) {
    return;
  }

  element.hidden = false;
  element.textContent = message;
  element.className = variant ? `status-banner ${variant}` : "status-banner";
}

function hideStatusBanner(element) {
  if (!element) {
    return;
  }

  element.hidden = true;
  element.textContent = "";
  element.className = "status-banner";
}

function getStoredStaffUser() {
  const rawUser = sessionStorage.getItem("foundItStaffUser");

  if (!rawUser) {
    return null;
  }

  try {
    return JSON.parse(rawUser);
  } catch {
    sessionStorage.removeItem("foundItStaffUser");
    return null;
  }
}

async function requestJson(url, options = {}) {
  const response = await fetch(url, options);
  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(payload.message || "Request failed");
  }

  return payload;
}

function formatListingDate(value) {
  if (!value) {
    return "Recently added";
  }

  const parsedDate = new Date(value);

  if (Number.isNaN(parsedDate.getTime())) {
    return "Recently added";
  }

  return parsedDate.toLocaleDateString("en-US", {
    month: "long",
    day: "numeric",
    year: "numeric",
  });
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function normalizeText(value) {
  return String(value ?? "").trim().toLowerCase();
}

function inferBrowseCategory(item) {
  const searchText = normalizeText(`${item.itemType} ${item.desc}`);

  if (
    /(backpack|bag|bookbag|duffel|tote|purse|satchel|luggage)/.test(searchText)
  ) {
    return "Bag";
  }

  if (
    /(charger|laptop|airpods|headphones|earbuds|phone|ipad|tablet|macbook|electronics|usb|camera)/.test(
      searchText,
    )
  ) {
    return "Electronics";
  }

  if (/(bottle|tumbler|hydro flask|mug|cup|thermos|drinkware)/.test(searchText)) {
    return "Drinkware";
  }

  if (
    /(hoodie|jacket|coat|shirt|pants|hat|cap|glove|clothing|shoe|sneaker)/.test(
      searchText,
    )
  ) {
    return "Clothing";
  }

  if (
    /(wallet|keys|keychain|lanyard|id|badge|glasses|watch|umbrella|accessory)/.test(
      searchText,
    )
  ) {
    return "Accessories";
  }

  return "Campus Listing";
}

function inferReturnDesk(location) {
  const normalizedLocation = normalizeText(location);

  if (normalizedLocation.includes("belk")) {
    return "Belk Library Front Desk";
  }

  if (
    normalizedLocation.includes("plemmons") ||
    normalizedLocation.includes("student union")
  ) {
    return "Plemmons Student Union Information Desk";
  }

  if (
    normalizedLocation.includes("rec center") ||
    normalizedLocation.includes("recreation")
  ) {
    return "Student Recreation Center Front Desk";
  }

  if (normalizedLocation.includes("peacock")) {
    return "Peacock Hall Main Office Front Desk";
  }

  if (
    normalizedLocation.includes("stadium") ||
    normalizedLocation.includes("kidd brewer")
  ) {
    return "Kidd Brewer Stadium Guest Services Desk";
  }

  return "Campus Front Desk";
}

function isRecentlyAdded(value) {
  const parsedDate = Date.parse(value || "");

  if (!parsedDate) {
    return false;
  }

  return Date.now() - parsedDate <= 1000 * 60 * 60 * 24 * 3;
}

function getBrowseSearchText(item) {
  return normalizeText(
    [
      item.itemType,
      item.desc,
      item.loc,
      inferBrowseCategory(item),
      inferReturnDesk(item.loc),
    ].join(" "),
  );
}

function getBrowseFilters() {
  if (!browseFiltersForm) {
    return {
      keyword: "",
      category: "",
      foundNear: "",
      returnTo: "",
      sort: "newest",
    };
  }

  const formData = new FormData(browseFiltersForm);

  return {
    keyword: normalizeText(formData.get("keyword")),
    category: normalizeText(formData.get("category")),
    foundNear: normalizeText(formData.get("foundNear")),
    returnTo: normalizeText(formData.get("returnTo")),
    sort: String(formData.get("sort") ?? "newest"),
  };
}

function getBrowseMatchScore(item, keywordTerms) {
  const searchText = getBrowseSearchText(item);

  return keywordTerms.reduce((score, term) => {
    return score + (searchText.includes(term) ? 1 : 0);
  }, 0);
}

function updateBrowseResultsMeta(visibleCount, totalCount) {
  if (!browseResultsMeta) {
    return;
  }

  if (!totalCount) {
    browseResultsMeta.textContent = "No live listings yet";
    return;
  }

  const noun = totalCount === 1 ? "listing" : "listings";

  browseResultsMeta.textContent =
    visibleCount === totalCount
      ? `${totalCount} live ${noun}`
      : `Showing ${visibleCount} of ${totalCount} ${noun}`;
}

function renderBrowseItems(
  items,
  emptyMessage = "No listings match those filters right now. Try clearing one or more filters.",
) {
  if (!itemGrid) {
    return;
  }

  if (!items.length) {
    itemGrid.innerHTML = `
      <div class="empty-state">
        ${escapeHtml(emptyMessage)}
      </div>
    `;
    return;
  }

  itemGrid.innerHTML = items
    .map((item) => {
      const title = escapeHtml(item.itemType || "Untitled item");
      const description = escapeHtml(item.desc || "No description provided.");
      const location = escapeHtml(item.loc || "Location not provided");
      const returnDesk = escapeHtml(inferReturnDesk(item.loc));
      const category = escapeHtml(inferBrowseCategory(item));
      const dateLabel = formatListingDate(item.date);
      const tags = [
        category,
        isRecentlyAdded(item.date) ? "Recently added" : null,
        "Ready to claim",
      ].filter(Boolean);

      return `
        <article class="item-card">
          <div class="item-description">
            <h3 class="item-title">${title}</h3>
            <p>${description}</p>
          </div>

          <div class="item-tags">
            ${tags
              .map((tag) => `<span class="item-tag">${escapeHtml(tag)}</span>`)
              .join("")}
          </div>

          <div class="item-meta">
            <div class="item-meta-row">
              <span class="meta-label">Found Near</span>
              <span class="meta-value">${location}</span>
            </div>
            <div class="item-meta-row">
              <span class="meta-label">Return To</span>
              <span class="meta-value">${returnDesk}</span>
            </div>
            <div class="item-meta-row">
              <span class="meta-label">Date Added</span>
              <span class="meta-value">${dateLabel}</span>
            </div>
          </div>
        </article>
      `;
    })
    .join("");
}

function applyBrowseFilters() {
  const filters = getBrowseFilters();
  const keywordTerms = filters.keyword.split(/\s+/).filter(Boolean);
  const hasActiveFilters = Boolean(
    filters.keyword || filters.category || filters.foundNear || filters.returnTo,
  );

  const filteredItems = browseItemsState.filter((item) => {
    const searchText = getBrowseSearchText(item);
    const category = normalizeText(inferBrowseCategory(item));
    const location = normalizeText(item.loc);
    const returnDesk = normalizeText(inferReturnDesk(item.loc));

    if (keywordTerms.length && !keywordTerms.every((term) => searchText.includes(term))) {
      return false;
    }

    if (filters.category && category !== filters.category) {
      return false;
    }

    if (filters.foundNear && !location.includes(filters.foundNear)) {
      return false;
    }

    if (filters.returnTo && !returnDesk.includes(filters.returnTo)) {
      return false;
    }

    return true;
  });

  const sortedItems = [...filteredItems].sort((left, right) => {
    const leftTime = Date.parse(left.date || "") || 0;
    const rightTime = Date.parse(right.date || "") || 0;

    if (filters.sort === "oldest") {
      return leftTime - rightTime;
    }

    if (filters.sort === "desk") {
      return inferReturnDesk(left.loc).localeCompare(inferReturnDesk(right.loc));
    }

    if (filters.sort === "match" && keywordTerms.length) {
      const scoreDifference =
        getBrowseMatchScore(right, keywordTerms) -
        getBrowseMatchScore(left, keywordTerms);

      return scoreDifference || rightTime - leftTime;
    }

    return rightTime - leftTime;
  });

  const emptyMessage = browseItemsState.length
    ? hasActiveFilters
      ? "No listings match those filters right now. Try clearing one or more filters."
      : "No live listings are available right now."
    : "No live listings have been posted yet.";

  renderBrowseItems(sortedItems, emptyMessage);
  updateBrowseResultsMeta(sortedItems.length, browseItemsState.length);
}

async function loadBrowseListings() {
  if (!itemGrid) {
    return;
  }

  updateStatusBanner(browseStatus, "Loading live listings...", "");

  try {
    const items = await requestJson("/api/items");
    browseItemsState = Array.isArray(items) ? items : [];
    hideStatusBanner(browseStatus);
    applyBrowseFilters();
  } catch (error) {
    browseItemsState = [];
    updateStatusBanner(
      browseStatus,
      error.message || "Unable to load live listings right now.",
      "is-error",
    );
    renderBrowseItems([], "Live listings are temporarily unavailable.");
    updateBrowseResultsMeta(0, 0);
  }
}

function renderAdminListings(items) {
  if (!adminListings) {
    return;
  }

  if (!items.length) {
    adminListings.innerHTML =
      '<p class="admin-simple-copy">No listings have been created yet.</p>';
    return;
  }

  adminListings.innerHTML = items
    .map((item) => {
      const title = escapeHtml(item.itemType || "Untitled item");
      const description = escapeHtml(item.desc || "No description provided.");
      const location = escapeHtml(item.loc || "Location not provided");
      const dateLabel = formatListingDate(item.date);
      const actionButton = item.id
        ? `<button type="button" class="btn btn-primary" data-delete-item-id="${item.id}">
            Mark Returned
          </button>`
        : "";

      return `
        <article class="admin-simple-listing">
          <div>
            <h3>${title}</h3>
            <p>${description}</p>
            <p>Found at ${location}. Added ${dateLabel}.</p>
          </div>
          <div class="admin-simple-actions">
            ${actionButton}
          </div>
        </article>
      `;
    })
    .join("");
}

async function loadAdminListings() {
  if (!adminListings) {
    return;
  }

  adminListings.innerHTML =
    '<p class="admin-simple-copy">Loading current listings...</p>';

  try {
    const items = await requestJson("/api/items");
    const sortedItems = [...items].sort((left, right) => {
      const leftTime = Date.parse(left.date || "") || 0;
      const rightTime = Date.parse(right.date || "") || 0;
      return rightTime - leftTime;
    });

    renderAdminListings(sortedItems);
  } catch (error) {
    adminListings.innerHTML = `<p class="admin-simple-copy">${
      error.message || "Unable to load current listings."
    }</p>`;
  }
}

staffSignInForm?.addEventListener("submit", async (event) => {
  event.preventDefault();

  const formData = new FormData(staffSignInForm);
  const username = String(formData.get("username") ?? "").trim();
  const password = String(formData.get("password") ?? "");

  updateStatusBanner(signInStatus, "Checking your credentials...", "is-success");

  try {
    const payload = await requestJson("/api/auth/signin", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ username, password }),
    });

    sessionStorage.setItem("foundItStaffUser", JSON.stringify(payload));
    window.location.href = "/admin.html";
  } catch (error) {
    updateStatusBanner(
      signInStatus,
      error.message || "Unable to sign in right now.",
      "is-error",
    );
  }
});

signOutLink?.addEventListener("click", () => {
  sessionStorage.removeItem("foundItStaffUser");
});

adminCreateForm?.addEventListener("submit", async (event) => {
  event.preventDefault();

  const staffUser = getStoredStaffUser();

  if (!staffUser?.id) {
    updateStatusBanner(
      adminStatus,
      "Your staff session has expired. Sign in again to create listings.",
      "is-error",
    );
    sessionStorage.removeItem("foundItStaffUser");
    window.setTimeout(() => {
      window.location.href = "/signin.html";
    }, 900);
    return;
  }

  hideStatusBanner(adminStatus);

  const formData = new FormData(adminCreateForm);
  const itemType = String(formData.get("itemType") ?? "").trim();
  const desc = String(formData.get("desc") ?? "").trim();
  const loc = String(formData.get("loc") ?? "").trim();

  updateStatusBanner(adminStatus, "Creating listing...", "is-success");

  try {
    await requestJson("/api/items", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        adminId: staffUser.id,
        itemType,
        desc,
        loc,
      }),
    });

    adminCreateForm.reset();
    updateStatusBanner(adminStatus, "Listing created successfully.", "is-success");
    await loadAdminListings();
  } catch (error) {
    updateStatusBanner(
      adminStatus,
      error.message || "Unable to create the listing.",
      "is-error",
    );
  }
});

adminListings?.addEventListener("click", async (event) => {
  const deleteButton =
    event.target instanceof Element
      ? event.target.closest("[data-delete-item-id]")
      : null;

  if (!deleteButton) {
    return;
  }

  const { deleteItemId } = deleteButton.dataset;

  if (!deleteItemId) {
    return;
  }

  deleteButton.disabled = true;

  try {
    await requestJson(`/api/items/${deleteItemId}`, {
      method: "DELETE",
    });
    updateStatusBanner(adminStatus, "Listing removed.", "is-success");
    await loadAdminListings();
  } catch (error) {
    updateStatusBanner(
      adminStatus,
      error.message || "Unable to remove the listing.",
      "is-error",
    );
    deleteButton.disabled = false;
  }
});

if (adminCreateForm) {
  const staffUser = getStoredStaffUser();

  if (!staffUser?.id) {
    sessionStorage.removeItem("foundItStaffUser");
    window.location.href = "/signin.html";
  } else {
    loadAdminListings();
  }
}

browseFiltersForm?.addEventListener("submit", (event) => {
  event.preventDefault();
  hideStatusBanner(browseStatus);
  applyBrowseFilters();
});

browseFiltersForm?.addEventListener("reset", () => {
  window.setTimeout(() => {
    hideStatusBanner(browseStatus);
    applyBrowseFilters();
  }, 0);
});

if (itemGrid) {
  loadBrowseListings();
}
