// Grab the bits of the page we actually use.
const dom = {
  navToggleButton: document.getElementById("nav-toggle-btn"),
  navAuthLink: document.getElementById("nav-auth-link"),
  navStaffLink: document.getElementById("nav-staff-link"),
  navClaimedLink: document.getElementById("nav-claimed-link"),
  signOutLink: document.getElementById("nav-signout-link"),

  staffSignInForm:
    document.getElementById("staff-signin-form") ??
    document.querySelector(".staff-signin-form"),
  signInStatus: document.getElementById("signin-status"),

  adminCreateForm: document.getElementById("admin-create-form"),
  adminStatus: document.getElementById("admin-status"),

  browseFiltersForm: document.getElementById("browseFilters"),
  itemGrid: document.getElementById("itemGrid"),
  browseStatus: document.getElementById("browse-status"),

  claimedGrid: document.getElementById("claimedGrid"),
  claimedStatus: document.getElementById("claimed-status"),

  claimModal: document.getElementById("claim-modal"),
  claimForm: document.getElementById("claim-form"),
  claimStatus: document.getElementById("claim-status"),
  claimModalItemCopy: document.getElementById("claim-modal-item-copy"),
  claimantTypeSelect: document.getElementById("claimant-type"),
  claimStudentFields: document.getElementById("claim-student-fields"),
  claimStudentIdInput: document.getElementById("claim-student-id"),
  claimGuestFields: document.getElementById("claim-guest-fields"),
  claimPhoneInput: document.getElementById("claim-phone"),
  claimEmailInput: document.getElementById("claim-email"),
};

const state = {
  activeStaffUser: null,
  browseItems: [],
};

// Small helper so links still work if the app is not served from "/".
function resolveAppUrl(path) {
  return new URL(path, window.location.href).toString();
}

function normalizeText(value) {
  return String(value ?? "").trim().toLowerCase();
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
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

function updateStatusBanner(element, message, variant = "") {
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

async function requestJson(url, options = {}) {
  const response = await fetch(url, options);
  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(payload.message || "Request failed");
  }

  return payload;
}

// Keep staff session in sessionStorage so refreshes feel normal.
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

function saveStaffUser(user) {
  sessionStorage.setItem("foundItStaffUser", JSON.stringify(user));
}

function clearStoredStaffUser() {
  sessionStorage.removeItem("foundItStaffUser");
}

function showPublicNavigation() {
  if (dom.navStaffLink) {
    dom.navStaffLink.hidden = true;
    dom.navStaffLink.href = resolveAppUrl("admin.html");
  }

  if (dom.navClaimedLink) {
    dom.navClaimedLink.hidden = true;
    dom.navClaimedLink.href = resolveAppUrl("claimed.html");
  }

  if (dom.signOutLink) {
    dom.signOutLink.hidden = true;
    dom.signOutLink.href = resolveAppUrl("index.html");
  }

  if (!dom.navAuthLink) {
    return;
  }

  dom.navAuthLink.textContent = "Sign In";
  dom.navAuthLink.href = resolveAppUrl("signin.html");
  dom.navAuthLink.classList.remove("nav-user-link");
  dom.navAuthLink.removeAttribute("title");
}

function showStaffNavigation(staffUser) {
  const username = String(staffUser?.username ?? "").trim();

  if (dom.navStaffLink) {
    dom.navStaffLink.hidden = false;
    dom.navStaffLink.href = resolveAppUrl("admin.html");
  }

  if (dom.navClaimedLink) {
    dom.navClaimedLink.hidden = false;
    dom.navClaimedLink.href = resolveAppUrl("claimed.html");
  }

  if (dom.signOutLink) {
    dom.signOutLink.hidden = false;
    dom.signOutLink.href = resolveAppUrl("index.html");
  }

  if (!dom.navAuthLink) {
    return;
  }

  dom.navAuthLink.textContent = username;
  dom.navAuthLink.href = resolveAppUrl("admin.html");
  dom.navAuthLink.classList.add("nav-user-link");
  dom.navAuthLink.title = `Signed in as ${username}`;
}

// Show what we have in storage first, then re-check it with the backend.
function paintStoredNavigation() {
  const storedUser = getStoredStaffUser();
  const storedId = String(storedUser?.id ?? "").trim();
  const storedUsername = String(storedUser?.username ?? "").trim();

  if (!storedId || !storedUsername) {
    showPublicNavigation();
    return null;
  }

  showStaffNavigation({
    id: storedId,
    username: storedUsername,
  });

  return storedUser;
}

async function verifyStoredStaffUser() {
  const storedUser = getStoredStaffUser();
  const storedId = String(storedUser?.id ?? "").trim();
  const storedUsername = String(storedUser?.username ?? "").trim();

  if (!storedId || !storedUsername) {
    clearStoredStaffUser();
    return null;
  }

  try {
    const adminRecord = await requestJson(resolveAppUrl(`api/admins/${storedId}`));
    const verifiedId = String(adminRecord?.id ?? "").trim();
    const verifiedUsername = String(adminRecord?.username ?? "").trim();

    if (!verifiedId) {
      clearStoredStaffUser();
      return null;
    }

    if (normalizeText(verifiedUsername) !== normalizeText(storedUsername)) {
      clearStoredStaffUser();
      return null;
    }

    const verifiedUser = {
      id: verifiedId,
      username: verifiedUsername,
      firstName: String(adminRecord?.firstName ?? ""),
      lastName: String(adminRecord?.lastName ?? ""),
    };

    saveStaffUser(verifiedUser);
    return verifiedUser;
  } catch {
    clearStoredStaffUser();
    return null;
  }
}

async function syncStaffNavigation() {
  paintStoredNavigation();

  const verifiedUser = await verifyStoredStaffUser();

  if (verifiedUser) {
    showStaffNavigation(verifiedUser);
    return verifiedUser;
  }

  showPublicNavigation();
  return null;
}

function redirectToSignInSoon() {
  window.setTimeout(() => {
    window.location.href = resolveAppUrl("signin.html");
  }, 900);
}

async function requireStaffUser(statusElement, message) {
  const staffUser = await verifyStoredStaffUser();

  if (staffUser?.id) {
    state.activeStaffUser = staffUser;
    showStaffNavigation(staffUser);
    return staffUser;
  }

  state.activeStaffUser = null;
  showPublicNavigation();

  if (statusElement && message) {
    updateStatusBanner(statusElement, message, "is-error");
  }

  redirectToSignInSoon();
  return null;
}

function createItemsByIdMap(items) {
  return new Map(
    items
      .filter((item) => item && item.id)
      .map((item) => [String(item.id), item]),
  );
}

function formatClaimType(claimRecord) {
  return claimRecord?.isAppUser ? "Student" : "Guest";
}

// If an item already has a category saved, use it.
// If not, take a best guess from the name/description.
function inferBrowseCategory(item) {
  const savedCategory = String(item?.category ?? "").trim();

  if (savedCategory) {
    return savedCategory;
  }

  const searchText = normalizeText(`${item?.itemType} ${item?.desc}`);

  if (
    /(charger|laptop|airpods|headphones|earbuds|phone|ipad|tablet|macbook|electronics|usb|camera)/.test(
      searchText,
    )
  ) {
    return "Electronics";
  }

  if (
    /(student id|id card|identification|license|passport|badge|credit card|debit card|gift card|app card|\bcards?\b)/.test(
      searchText,
    )
  ) {
    return "IDs & Cards";
  }

  if (/(backpack|bag|bookbag|duffel|tote|purse|satchel|luggage)/.test(searchText)) {
    return "Bags";
  }

  if (/(wallet|keys|keychain|key fob|fob|lanyard)/.test(searchText)) {
    return "Keys & Wallets";
  }

  if (
    /(hoodie|jacket|coat|shirt|pants|hat|cap|glove|clothing|shoe|sneaker)/.test(
      searchText,
    )
  ) {
    return "Clothing";
  }

  if (
    /(notebook|textbook|book|binder|folder|planner|calculator|pen|pencil|eraser|highlighter|marker|school supplies?)/.test(
      searchText,
    )
  ) {
    return "School Supplies";
  }

  if (
    /(bottle|tumbler|hydro flask|mug|cup|thermos|glasses|sunglasses|watch|umbrella|jewelry|ring|bracelet|necklace|makeup|cosmetic|toiletry|personal item)/.test(
      searchText,
    )
  ) {
    return "Personal Items";
  }

  return "Other";
}

function inferReturnDesk(location) {
  const text = normalizeText(location);

  if (text.includes("belk")) {
    return "Belk Library Front Desk";
  }

  if (text.includes("plemmons") || text.includes("student union")) {
    return "Plemmons Student Union Information Desk";
  }

  if (text.includes("rec center") || text.includes("recreation")) {
    return "Student Recreation Center Front Desk";
  }

  if (text.includes("peacock")) {
    return "Peacock Hall Main Office Front Desk";
  }

  if (text.includes("stadium") || text.includes("kidd brewer")) {
    return "Kidd Brewer Stadium Guest Services Desk";
  }

  return "Campus Front Desk";
}

function getBrowseReturnDesk(item) {
  const savedDesk = String(item?.returnTo ?? "").trim();
  return savedDesk || inferReturnDesk(item?.loc);
}

function getBrowseFilters() {
  if (!dom.browseFiltersForm) {
    return {
      category: "",
      foundNear: "",
      returnTo: "",
    };
  }

  const formData = new FormData(dom.browseFiltersForm);

  return {
    category: normalizeText(formData.get("category")),
    foundNear: normalizeText(formData.get("foundNear")),
    returnTo: normalizeText(formData.get("returnTo")),
  };
}

function renderBrowseItems(
  items,
  emptyMessage = "No listings match those filters right now. Try clearing one or more filters.",
) {
  if (!dom.itemGrid) {
    return;
  }

  if (!items.length) {
    dom.itemGrid.innerHTML = `
      <div class="empty-state">
        ${escapeHtml(emptyMessage)}
      </div>
    `;
    return;
  }

  dom.itemGrid.innerHTML = items
    .map((item) => {
      const title = escapeHtml(item.itemType || "Untitled item");
      const description = escapeHtml(item.desc || "No description provided.");
      const location = escapeHtml(item.loc || "Location not provided");
      const returnDesk = escapeHtml(getBrowseReturnDesk(item));
      const category = escapeHtml(inferBrowseCategory(item));
      const dateLabel = escapeHtml(formatListingDate(item.date));

      const claimButton =
        state.activeStaffUser?.id && item.id
          ? `
              <div class="item-actions">
                <button
                  type="button"
                  class="btn btn-secondary"
                  data-start-claim-id="${escapeHtml(item.id)}"
                >
                  Start Claim
                </button>
              </div>
            `
          : "";

      return `
        <article class="item-card">
          <div class="item-description">
            <h3 class="item-title">${title}</h3>
            <p>${description}</p>
          </div>

          <div class="item-tags">
            <span class="item-tag">${category}</span>
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

          ${claimButton}
        </article>
      `;
    })
    .join("");
}

function renderClaimedItems(
  claims,
  itemsById,
  emptyMessage = "No claimed items have been saved yet.",
) {
  if (!dom.claimedGrid) {
    return;
  }

  if (!claims.length) {
    dom.claimedGrid.innerHTML = `
      <div class="empty-state">
        ${escapeHtml(emptyMessage)}
      </div>
    `;
    return;
  }

  dom.claimedGrid.innerHTML = claims
    .map((claimRecord) => {
      const item = itemsById.get(String(claimRecord?.itemId ?? ""));
      const title = escapeHtml(item?.itemType || "Claimed item");
      const description = escapeHtml(
        item?.desc || "The original listing details are no longer available.",
      );
      const category = escapeHtml(item ? inferBrowseCategory(item) : "Claim record");
      const foundNear = escapeHtml(item?.loc || "No location on file");
      const returnDesk = escapeHtml(
        item ? getBrowseReturnDesk(item) : "No return desk on file",
      );
      const listingDate = escapeHtml(formatListingDate(item?.date));
      const claimDate = escapeHtml(formatListingDate(claimRecord?.date));
      const claimType = escapeHtml(formatClaimType(claimRecord));
      const claimantName = escapeHtml(
        [claimRecord?.firstName, claimRecord?.lastName]
          .filter(Boolean)
          .join(" ")
          .trim() || "Unknown claimer",
      );

      const studentIdRow = claimRecord?.isAppUser
        ? `
            <div class="item-meta-row">
              <span class="meta-label">Student ID</span>
              <span class="meta-value">${escapeHtml(claimRecord.appId || "Not provided")}</span>
            </div>
          `
        : "";

      const phoneRow = !claimRecord?.isAppUser
        ? `
            <div class="item-meta-row">
              <span class="meta-label">Phone</span>
              <span class="meta-value">${escapeHtml(claimRecord.phoneNum || "Not provided")}</span>
            </div>
          `
        : "";

      const emailRow = !claimRecord?.isAppUser
        ? `
            <div class="item-meta-row">
              <span class="meta-label">Email</span>
              <span class="meta-value">${escapeHtml(claimRecord.email || "Not provided")}</span>
            </div>
          `
        : "";

      return `
        <article class="item-card">
          <div class="item-description">
            <h3 class="item-title">${title}</h3>
            <p>${description}</p>
          </div>

          <div class="item-tags">
            <span class="item-tag">${category}</span>
          </div>

          <div class="claim-summary claim-summary-inline">
            <p><strong>Claimed By:</strong> ${claimantName}</p>
            <p>${claimType} claimant. Claim saved ${claimDate}.</p>
          </div>

          <div class="item-meta">
            <div class="item-meta-row">
              <span class="meta-label">Found Near</span>
              <span class="meta-value">${foundNear}</span>
            </div>
            <div class="item-meta-row">
              <span class="meta-label">Return To</span>
              <span class="meta-value">${returnDesk}</span>
            </div>
            <div class="item-meta-row">
              <span class="meta-label">Listing Added</span>
              <span class="meta-value">${listingDate}</span>
            </div>
            ${studentIdRow}
            ${phoneRow}
            ${emailRow}
          </div>
        </article>
      `;
    })
    .join("");
}

function applyBrowseFilters() {
  const filters = getBrowseFilters();

  const filteredItems = state.browseItems.filter((item) => {
    const category = normalizeText(inferBrowseCategory(item));
    const location = normalizeText(item.loc);
    const returnDesk = normalizeText(getBrowseReturnDesk(item));

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

  filteredItems.sort((left, right) => {
    const leftTime = Date.parse(left?.date || "") || 0;
    const rightTime = Date.parse(right?.date || "") || 0;
    return rightTime - leftTime;
  });

  const emptyMessage = state.browseItems.length
    ? "No listings match those filters right now. Try clearing one or more filters."
    : "No listings have been posted yet.";

  renderBrowseItems(filteredItems, emptyMessage);
}

async function loadBrowseListings() {
  if (!dom.itemGrid) {
    return;
  }

  updateStatusBanner(dom.browseStatus, "Loading listings...");

  try {
    const items = await requestJson(resolveAppUrl("api/items/unclaimed"));
    state.browseItems = Array.isArray(items) ? items : [];
    hideStatusBanner(dom.browseStatus);
    applyBrowseFilters();
  } catch (error) {
    state.browseItems = [];
    updateStatusBanner(
      dom.browseStatus,
      error.message || "Unable to load listings right now.",
      "is-error",
    );
    renderBrowseItems([], "Listings are temporarily unavailable.");
  }
}

async function loadClaimedListings() {
  if (!dom.claimedGrid) {
    return;
  }

  updateStatusBanner(dom.claimedStatus, "Loading claimed items...");

  try {
    const [items, claims] = await Promise.all([
      requestJson(resolveAppUrl("api/items")),
      requestJson(resolveAppUrl("api/foundItems")),
    ]);

    const itemsById = createItemsByIdMap(Array.isArray(items) ? items : []);
    const claimedRecords = Array.isArray(claims) ? claims : [];

    claimedRecords.sort((left, right) => {
      const leftTime = Date.parse(left?.date || "") || 0;
      const rightTime = Date.parse(right?.date || "") || 0;
      return rightTime - leftTime;
    });

    hideStatusBanner(dom.claimedStatus);
    renderClaimedItems(claimedRecords, itemsById);
  } catch (error) {
    updateStatusBanner(
      dom.claimedStatus,
      error.message || "Unable to load claimed items right now.",
      "is-error",
    );
    renderClaimedItems([], new Map(), "Claimed items are temporarily unavailable.");
  }
}

function updateClaimFormState() {
  if (!dom.claimantTypeSelect) {
    return;
  }

  const isStudent = dom.claimantTypeSelect.value !== "guest";

  if (dom.claimStudentFields) {
    dom.claimStudentFields.hidden = !isStudent;
  }

  if (dom.claimGuestFields) {
    dom.claimGuestFields.hidden = isStudent;
  }

  if (dom.claimStudentIdInput) {
    dom.claimStudentIdInput.required = isStudent;
  }

  // Both claim paths keep contact info now.
  if (dom.claimPhoneInput) {
    dom.claimPhoneInput.required = true;
  }

  if (dom.claimEmailInput) {
    dom.claimEmailInput.required = true;
  }
}

function closeClaimModal() {
  if (!dom.claimModal) {
    return;
  }

  dom.claimModal.hidden = true;
  dom.claimForm?.reset();
  hideStatusBanner(dom.claimStatus);

  if (dom.claimantTypeSelect) {
    dom.claimantTypeSelect.value = "student";
  }

  updateClaimFormState();
}

// Same modal every time. We just swap the item copy and hidden id.
function openClaimModal(item) {
  if (!dom.claimModal || !dom.claimForm || !item?.id) {
    return;
  }

  dom.claimForm.reset();
  hideStatusBanner(dom.claimStatus);

  const itemIdField = dom.claimForm.elements.namedItem("itemId");
  if (itemIdField instanceof HTMLInputElement) {
    itemIdField.value = item.id;
  }

  if (dom.claimModalItemCopy) {
    const title = item.itemType || "Selected item";
    const location = item.loc || "Unknown location";
    dom.claimModalItemCopy.textContent = `Collect claimer details for ${title}, found at ${location}.`;
  }

  if (dom.claimantTypeSelect) {
    dom.claimantTypeSelect.value = "student";
  }

  updateClaimFormState();
  dom.claimModal.hidden = false;
}

async function handleStaffSignIn(event) {
  event.preventDefault();

  if (!dom.staffSignInForm) {
    return;
  }

  const formData = new FormData(dom.staffSignInForm);
  const username = String(formData.get("username") ?? "").trim();
  const password = String(formData.get("password") ?? "");

  updateStatusBanner(dom.signInStatus, "Checking your credentials...", "is-success");

  try {
    const payload = await requestJson(resolveAppUrl("api/auth/signin"), {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ username, password }),
    });

    saveStaffUser(payload);
    window.location.href = resolveAppUrl("index.html");
  } catch (error) {
    updateStatusBanner(
      dom.signInStatus,
      error.message || "Unable to sign in right now.",
      "is-error",
    );
  }
}

function handleSignOut(event) {
  event.preventDefault();
  clearStoredStaffUser();
  state.activeStaffUser = null;
  showPublicNavigation();
  window.location.href = resolveAppUrl("index.html");
}

async function handleAdminCreate(event) {
  event.preventDefault();

  const staffUser = await requireStaffUser(
    dom.adminStatus,
    "Your staff session has expired. Sign in again to create listings.",
  );

  if (!staffUser || !dom.adminCreateForm) {
    return;
  }

  hideStatusBanner(dom.adminStatus);

  const formData = new FormData(dom.adminCreateForm);
  const itemType = String(formData.get("itemType") ?? "").trim();
  const desc = String(formData.get("desc") ?? "").trim();
  const category = String(formData.get("category") ?? "").trim();
  const loc = String(formData.get("loc") ?? "").trim();
  const returnTo = String(formData.get("returnTo") ?? "").trim();

  updateStatusBanner(dom.adminStatus, "Creating listing...", "is-success");

  try {
    await requestJson(resolveAppUrl("api/items"), {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        adminId: staffUser.id,
        itemType,
        desc,
        category,
        loc,
        returnTo,
      }),
    });

    dom.adminCreateForm.reset();
    updateStatusBanner(dom.adminStatus, "Listing created successfully.", "is-success");
  } catch (error) {
    updateStatusBanner(
      dom.adminStatus,
      error.message || "Unable to create the listing.",
      "is-error",
    );
  }
}

function handleItemGridClick(event) {
  if (!(event.target instanceof Element)) {
    return;
  }

  const claimButton = event.target.closest("[data-start-claim-id]");

  if (!claimButton) {
    return;
  }

  const itemId = String(claimButton.getAttribute("data-start-claim-id") ?? "");

  if (!itemId) {
    return;
  }

  const selectedItem = state.browseItems.find((item) => String(item.id) === itemId);
  openClaimModal(selectedItem);
}

async function handleClaimSubmit(event) {
  event.preventDefault();

  const staffUser = await requireStaffUser(
    dom.claimStatus,
    "Your staff session has expired. Sign in again to start a claim.",
  );

  if (!staffUser || !dom.claimForm) {
    return;
  }

  const formData = new FormData(dom.claimForm);
  const claimantType = String(formData.get("claimantType") ?? "student");
  const itemId = String(formData.get("itemId") ?? "").trim();
  const firstName = String(formData.get("firstName") ?? "").trim();
  const lastName = String(formData.get("lastName") ?? "").trim();
  const studentId = String(formData.get("studentId") ?? "").trim();
  const phoneNum = String(formData.get("phoneNum") ?? "").trim();
  const email = String(formData.get("email") ?? "").trim();
  const isStudent = claimantType !== "guest";

  if (!itemId) {
    updateStatusBanner(dom.claimStatus, "Choose a listing before starting a claim.", "is-error");
    return;
  }

  updateStatusBanner(dom.claimStatus, "Saving claim details...", "is-success");

  try {
    await requestJson(resolveAppUrl("api/foundItems"), {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        itemId,
        firstName,
        lastName,
        isAppUser: isStudent,
        appId: isStudent ? studentId : "",
        phoneNum,
        email,
      }),
    });

    closeClaimModal();
    updateStatusBanner(
      dom.browseStatus,
      "Claim saved. The listing moved to the Claimed page.",
      "is-success",
    );
    await loadBrowseListings();
  } catch (error) {
    updateStatusBanner(
      dom.claimStatus,
      error.message || "Unable to start the claim.",
      "is-error",
    );
  }
}

function bindEvents() {
  dom.navToggleButton?.addEventListener("click", () => {
    document.body.classList.toggle("nav-open");
  });

  dom.staffSignInForm?.addEventListener("submit", handleStaffSignIn);
  dom.signOutLink?.addEventListener("click", handleSignOut);
  dom.adminCreateForm?.addEventListener("submit", handleAdminCreate);
  dom.itemGrid?.addEventListener("click", handleItemGridClick);
  dom.claimantTypeSelect?.addEventListener("change", updateClaimFormState);
  dom.claimForm?.addEventListener("submit", handleClaimSubmit);

  document.querySelectorAll("[data-close-claim-modal]").forEach((element) => {
    element.addEventListener("click", closeClaimModal);
  });

  dom.browseFiltersForm?.addEventListener("submit", (event) => {
    event.preventDefault();
    hideStatusBanner(dom.browseStatus);
    applyBrowseFilters();
  });

  dom.browseFiltersForm?.addEventListener("reset", () => {
    // Let the browser reset the form first, then re-render.
    window.setTimeout(() => {
      hideStatusBanner(dom.browseStatus);
      applyBrowseFilters();
    }, 0);
  });
}

async function initializePage() {
  state.activeStaffUser = await syncStaffNavigation();
  updateClaimFormState();

  const isStaffOnlyPage = Boolean(dom.adminCreateForm || dom.claimedGrid);

  if (isStaffOnlyPage && !state.activeStaffUser?.id) {
    window.location.href = resolveAppUrl("signin.html");
    return;
  }

  if (dom.itemGrid) {
    await loadBrowseListings();
  }

  if (dom.claimedGrid) {
    await loadClaimedListings();
  }
}

bindEvents();
initializePage();
