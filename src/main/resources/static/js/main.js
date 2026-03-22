const navToggleButton = document.getElementById("nav-toggle-btn");
const staffSignInForm =
  document.getElementById("staff-signin-form") ??
  document.querySelector(".staff-signin-form");
const signInStatus = document.getElementById("signin-status");
const adminCreateForm = document.getElementById("admin-create-form");
const adminStatus = document.getElementById("admin-status");
const adminListings = document.getElementById("admin-listings");
const signOutLink = document.getElementById("staff-signout-link");

navToggleButton?.addEventListener("click", () => {
  document.body.classList.toggle("nav-open");
});

function updateStatusBanner(element, message, variant) {
  if (!element) {
    return;
  }

  element.hidden = false;
  element.textContent = message;
  element.className = `status-banner ${variant}`;
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
