const navToggleButton = document.getElementById("nav-toggle-btn");
const staffSignInForm =
  document.getElementById("staff-signin-form") ??
  document.querySelector(".staff-signin-form");
const signInStatus = document.getElementById("signin-status");
const adminCreateForm = document.getElementById("admin-create-form");
const adminStatus = document.getElementById("admin-status");
const claimModal = document.getElementById("claim-modal");
const claimForm = document.getElementById("claim-form");
const claimStatus = document.getElementById("claim-status");
const claimModalItemCopy = document.getElementById("claim-modal-item-copy");
const claimantTypeSelect = document.getElementById("claimant-type");
const claimStudentFields = document.getElementById("claim-student-fields");
const claimStudentIdInput = document.getElementById("claim-student-id");
const claimGuestFields = document.getElementById("claim-guest-fields");
const claimGuestVerificationInput = document.getElementById(
  "claim-guest-verification",
);
const claimPhoneInput = document.getElementById("claim-phone");
const claimEmailInput = document.getElementById("claim-email");
const navAuthLink = document.getElementById("nav-auth-link");
const navStaffLink = document.getElementById("nav-staff-link");
const navClaimedLink = document.getElementById("nav-claimed-link");
const signOutLink = document.getElementById("nav-signout-link");
const browseFiltersForm = document.getElementById("browseFilters");
const itemGrid = document.getElementById("itemGrid");
const browseStatus = document.getElementById("browse-status");
const browseResultsMeta = document.getElementById("browse-results-meta");
const claimedGrid = document.getElementById("claimedGrid");
const claimedStatus = document.getElementById("claimed-status");

let browseItemsState = [];
let activeStaffUser = null;

function resolveAppUrl(path) {
  return new URL(path, window.location.href).toString();
}

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

function clearStoredStaffUser() {
  sessionStorage.removeItem("foundItStaffUser");
}

function setPublicNavigation() {
  if (navStaffLink) {
    navStaffLink.hidden = true;
    navStaffLink.href = resolveAppUrl("admin.html");
  }

  if (navClaimedLink) {
    navClaimedLink.hidden = true;
    navClaimedLink.href = resolveAppUrl("claimed.html");
  }

  if (signOutLink) {
    signOutLink.hidden = true;
    signOutLink.href = resolveAppUrl("index.html");
  }

  if (!navAuthLink) {
    return;
  }

  navAuthLink.textContent = "Sign In";
  navAuthLink.href = resolveAppUrl("signin.html");
  navAuthLink.classList.remove("nav-user-link");
  navAuthLink.removeAttribute("title");
}

function setStaffNavigation(staffUser) {
  const username = String(staffUser?.username ?? "").trim();

  if (navStaffLink) {
    navStaffLink.hidden = false;
    navStaffLink.href = resolveAppUrl("admin.html");
  }

  if (navClaimedLink) {
    navClaimedLink.hidden = false;
    navClaimedLink.href = resolveAppUrl("claimed.html");
  }

  if (signOutLink) {
    signOutLink.hidden = false;
    signOutLink.href = resolveAppUrl("index.html");
  }

  if (!navAuthLink) {
    return;
  }

  navAuthLink.textContent = username;
  navAuthLink.href = resolveAppUrl("admin.html");
  navAuthLink.classList.add("nav-user-link");
  navAuthLink.title = `Signed in as ${username}`;
}

function applyStoredStaffNavigation() {
  const storedUser = getStoredStaffUser();
  const storedId = String(storedUser?.id ?? "").trim();
  const storedUsername = String(storedUser?.username ?? "").trim();

  if (!storedId || !storedUsername) {
    setPublicNavigation();
    return null;
  }

  setStaffNavigation({
    id: storedId,
    username: storedUsername,
  });

  return storedUser;
}

async function validateStoredStaffUser() {
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

    if (!verifiedId || normalizeText(verifiedUsername) !== normalizeText(storedUsername)) {
      clearStoredStaffUser();
      return null;
    }

    const verifiedUser = {
      id: verifiedId,
      username: verifiedUsername,
      firstName: String(adminRecord?.firstName ?? ""),
      lastName: String(adminRecord?.lastName ?? ""),
    };

    sessionStorage.setItem("foundItStaffUser", JSON.stringify(verifiedUser));
    return verifiedUser;
  } catch {
    clearStoredStaffUser();
    return null;
  }
}

async function updateStaffNavigation() {
  applyStoredStaffNavigation();
  const verifiedUser = await validateStoredStaffUser();

  if (verifiedUser) {
    setStaffNavigation(verifiedUser);
    return verifiedUser;
  }

  setPublicNavigation();
  return null;
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

function formatClaimType(claimRecord) {
  return claimRecord?.isAppUser ? "Student" : "Guest";
}

function formatClaimSummary(claimRecord) {
  if (!claimRecord) {
    return "";
  }

  const claimantName = [claimRecord.firstName, claimRecord.lastName]
    .filter(Boolean)
    .join(" ")
    .trim();
  const claimType = formatClaimType(claimRecord);
  const detail = claimRecord?.isAppUser
    ? `Student ID on file: ${escapeHtml(claimRecord.appId || "Not provided")}.`
    : "Guest contact details are on file. Check a real ID at pickup.";

  return `
    <div class="claim-summary">
      <p><strong>Claim in progress:</strong> ${escapeHtml(claimantName || "Unknown claimer")}</p>
      <p>${escapeHtml(claimType)} claimant. ${detail}</p>
    </div>
  `;
}

function updateClaimFormState() {
  if (!claimantTypeSelect) {
    return;
  }

  const isStudent = claimantTypeSelect.value !== "guest";

  if (claimStudentFields) {
    claimStudentFields.hidden = !isStudent;
  }

  if (claimGuestFields) {
    claimGuestFields.hidden = isStudent;
  }

  if (claimStudentIdInput) {
    claimStudentIdInput.required = isStudent;
  }

  if (claimGuestVerificationInput) {
    claimGuestVerificationInput.required = !isStudent;
    if (isStudent) {
      claimGuestVerificationInput.checked = false;
    }
  }

  if (claimPhoneInput) {
    claimPhoneInput.required = !isStudent;
  }

  if (claimEmailInput) {
    claimEmailInput.required = !isStudent;
  }
}

function closeClaimModal() {
  if (!claimModal) {
    return;
  }

  claimModal.hidden = true;
  claimForm?.reset();
  hideStatusBanner(claimStatus);

  if (claimantTypeSelect) {
    claimantTypeSelect.value = "student";
  }

  updateClaimFormState();
}

function openClaimModal(item) {
  if (!claimModal || !claimForm || !item?.id) {
    return;
  }

  claimForm.reset();
  hideStatusBanner(claimStatus);

  const itemIdField = claimForm.elements.namedItem("itemId");
  if (itemIdField instanceof HTMLInputElement) {
    itemIdField.value = item.id;
  }

  if (claimModalItemCopy) {
    const title = item.itemType || "Selected item";
    const location = item.loc || "Unknown location";
    claimModalItemCopy.textContent = `Collect claimer details for ${title}, found at ${location}.`;
  }

  if (claimantTypeSelect) {
    claimantTypeSelect.value = "student";
  }

  updateClaimFormState();
  claimModal.hidden = false;
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function createItemsByIdMap(items) {
  return new Map(
    items
      .filter((item) => item && item.id)
      .map((item) => [String(item.id), item]),
  );
}

function normalizeText(value) {
  return String(value ?? "").trim().toLowerCase();
}

function inferBrowseCategory(item) {
  const savedCategory = String(item?.category ?? "").trim();

  if (savedCategory) {
    return savedCategory;
  }

  const searchText = normalizeText(`${item.itemType} ${item.desc}`);

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

  if (
    /(backpack|bag|bookbag|duffel|tote|purse|satchel|luggage)/.test(searchText)
  ) {
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

function getBrowseReturnDesk(item) {
  const savedReturnDesk = String(item?.returnTo ?? "").trim();

  if (savedReturnDesk) {
    return savedReturnDesk;
  }

  return inferReturnDesk(item?.loc);
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
      getBrowseReturnDesk(item),
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
    browseResultsMeta.textContent = "No listings yet";
    return;
  }

  const noun = totalCount === 1 ? "listing" : "listings";

  browseResultsMeta.textContent =
    visibleCount === totalCount
      ? `${totalCount} ${noun}`
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
      const returnDesk = escapeHtml(getBrowseReturnDesk(item));
      const category = escapeHtml(inferBrowseCategory(item));
      const dateLabel = formatListingDate(item.date);
      const claimButton =
        activeStaffUser?.id && item.id
          ? `<button type="button" class="btn btn-secondary" data-start-claim-id="${escapeHtml(item.id)}">
              Start Claim
            </button>`
          : "";
      const tags = [category].filter(Boolean);

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
          ${claimButton ? `<div class="item-actions">${claimButton}</div>` : ""}
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
  if (!claimedGrid) {
    return;
  }

  if (!claims.length) {
    claimedGrid.innerHTML = `
      <div class="empty-state">
        ${escapeHtml(emptyMessage)}
      </div>
    `;
    return;
  }

  claimedGrid.innerHTML = claims
    .map((claimRecord) => {
      const item = itemsById.get(String(claimRecord?.itemId ?? ""));
      const title = escapeHtml(item?.itemType || "Claimed item");
      const description = escapeHtml(
        item?.desc || "The original listing details are no longer available.",
      );
      const category = item ? escapeHtml(inferBrowseCategory(item)) : "Claim record";
      const foundNear = escapeHtml(item?.loc || "No location on file");
      const returnDesk = escapeHtml(
        item ? getBrowseReturnDesk(item) : "No return desk on file",
      );
      const listingDate = formatListingDate(item?.date);
      const claimDate = formatListingDate(claimRecord?.date);
      const claimantName = escapeHtml(
        [claimRecord?.firstName, claimRecord?.lastName].filter(Boolean).join(" ").trim() ||
          "Unknown claimer",
      );
      const claimType = escapeHtml(formatClaimType(claimRecord));
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

          <div class="claim-summary">
            <p><strong>Claimed By:</strong> ${claimantName}</p>
            <p>${claimType} claimant. Claim saved ${escapeHtml(claimDate)}.</p>
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
              <span class="meta-value">${escapeHtml(listingDate)}</span>
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
  const keywordTerms = filters.keyword.split(/\s+/).filter(Boolean);
  const hasActiveFilters = Boolean(
    filters.keyword || filters.category || filters.foundNear || filters.returnTo,
  );

  const filteredItems = browseItemsState.filter((item) => {
    const searchText = getBrowseSearchText(item);
    const category = normalizeText(inferBrowseCategory(item));
    const location = normalizeText(item.loc);
    const returnDesk = normalizeText(getBrowseReturnDesk(item));

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
      : "No listings are available right now."
    : "No listings have been posted yet.";

  renderBrowseItems(sortedItems, emptyMessage);
  updateBrowseResultsMeta(sortedItems.length, browseItemsState.length);
}

async function loadBrowseListings() {
  if (!itemGrid) {
    return;
  }

  updateStatusBanner(browseStatus, "Loading listings...", "");

  try {
    const items = await requestJson(resolveAppUrl("api/items/unclaimed"));
    browseItemsState = Array.isArray(items) ? items : [];
    hideStatusBanner(browseStatus);
    applyBrowseFilters();
  } catch (error) {
    browseItemsState = [];
    updateStatusBanner(
      browseStatus,
      error.message || "Unable to load listings right now.",
      "is-error",
    );
    renderBrowseItems([], "Listings are temporarily unavailable.");
    updateBrowseResultsMeta(0, 0);
  }
}

async function loadClaimedListings() {
  if (!claimedGrid) {
    return;
  }

  updateStatusBanner(claimedStatus, "Loading claimed items...", "");

  try {
    const [items, claims] = await Promise.all([
      requestJson(resolveAppUrl("api/items")),
      requestJson(resolveAppUrl("api/foundItems")),
    ]);
    const itemsById = createItemsByIdMap(Array.isArray(items) ? items : []);
    const claimedRecords = (Array.isArray(claims) ? claims : []).sort((left, right) => {
      const leftTime = Date.parse(left?.date || "") || 0;
      const rightTime = Date.parse(right?.date || "") || 0;
      return rightTime - leftTime;
    });

    hideStatusBanner(claimedStatus);
    renderClaimedItems(claimedRecords, itemsById);
  } catch (error) {
    updateStatusBanner(
      claimedStatus,
      error.message || "Unable to load claimed items right now.",
      "is-error",
    );
    renderClaimedItems([], new Map(), "Claimed items are temporarily unavailable.");
  }
}

staffSignInForm?.addEventListener("submit", async (event) => {
  event.preventDefault();

  const formData = new FormData(staffSignInForm);
  const username = String(formData.get("username") ?? "").trim();
  const password = String(formData.get("password") ?? "");

  updateStatusBanner(signInStatus, "Checking your credentials...", "is-success");

  try {
    const payload = await requestJson(resolveAppUrl("api/auth/signin"), {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ username, password }),
    });

    sessionStorage.setItem("foundItStaffUser", JSON.stringify(payload));
    window.location.href = resolveAppUrl("index.html");
  } catch (error) {
    updateStatusBanner(
      signInStatus,
      error.message || "Unable to sign in right now.",
      "is-error",
    );
  }
});

signOutLink?.addEventListener("click", (event) => {
  event.preventDefault();
  clearStoredStaffUser();
  setPublicNavigation();
  window.location.href = resolveAppUrl("index.html");
});

adminCreateForm?.addEventListener("submit", async (event) => {
  event.preventDefault();

  const staffUser = await validateStoredStaffUser();

  if (!staffUser?.id) {
    updateStatusBanner(
      adminStatus,
      "Your staff session has expired. Sign in again to create listings.",
      "is-error",
    );
    setPublicNavigation();
    window.setTimeout(() => {
      window.location.href = resolveAppUrl("signin.html");
    }, 900);
    return;
  }

  hideStatusBanner(adminStatus);

  const formData = new FormData(adminCreateForm);
  const itemType = String(formData.get("itemType") ?? "").trim();
  const desc = String(formData.get("desc") ?? "").trim();
  const category = String(formData.get("category") ?? "").trim();
  const loc = String(formData.get("loc") ?? "").trim();
  const returnTo = String(formData.get("returnTo") ?? "").trim();

  updateStatusBanner(adminStatus, "Creating listing...", "is-success");

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

    adminCreateForm.reset();
    updateStatusBanner(adminStatus, "Listing created successfully.", "is-success");
  } catch (error) {
    updateStatusBanner(
      adminStatus,
      error.message || "Unable to create the listing.",
      "is-error",
    );
  }
});

itemGrid?.addEventListener("click", async (event) => {
  const claimButton =
    event.target instanceof Element
      ? event.target.closest("[data-start-claim-id]")
      : null;

  if (claimButton) {
    const { startClaimId } = claimButton.dataset;

    if (!startClaimId) {
      return;
    }

    const selectedItem = browseItemsState.find((item) => String(item.id) === startClaimId);
    openClaimModal(selectedItem);
    return;
  }
});

claimantTypeSelect?.addEventListener("change", () => {
  updateClaimFormState();
});

claimForm?.addEventListener("submit", async (event) => {
  event.preventDefault();

  const staffUser = await validateStoredStaffUser();

  if (!staffUser?.id) {
    updateStatusBanner(
      claimStatus,
      "Your staff session has expired. Sign in again to start a claim.",
      "is-error",
    );
    setPublicNavigation();
    window.setTimeout(() => {
      window.location.href = resolveAppUrl("signin.html");
    }, 900);
    return;
  }

  const formData = new FormData(claimForm);
  const claimantType = String(formData.get("claimantType") ?? "student");
  const itemId = String(formData.get("itemId") ?? "").trim();
  const firstName = String(formData.get("firstName") ?? "").trim();
  const lastName = String(formData.get("lastName") ?? "").trim();
  const studentId = String(formData.get("studentId") ?? "").trim();
  const phoneNum = String(formData.get("phoneNum") ?? "").trim();
  const email = String(formData.get("email") ?? "").trim();
  const guestVerification = formData.get("guestVerification");
  const isStudent = claimantType !== "guest";

  if (!itemId) {
    updateStatusBanner(claimStatus, "Choose a listing before starting a claim.", "is-error");
    return;
  }

  if (!isStudent && !guestVerification) {
    updateStatusBanner(
      claimStatus,
      "Verify the guest's real ID before saving the claim.",
      "is-error",
    );
    return;
  }

  updateStatusBanner(claimStatus, "Saving claim details...", "is-success");

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
      browseStatus,
      "Claim saved. The listing moved to the Claimed page.",
      "is-success",
    );
    await loadBrowseListings();
  } catch (error) {
    updateStatusBanner(
      claimStatus,
      error.message || "Unable to start the claim.",
      "is-error",
    );
  }
});

document.querySelectorAll("[data-close-claim-modal]").forEach((element) => {
  element.addEventListener("click", () => {
    closeClaimModal();
  });
});

async function initializePage() {
  const verifiedStaffUser = await updateStaffNavigation();
  activeStaffUser = verifiedStaffUser;
  updateClaimFormState();

  if (adminCreateForm || claimedGrid) {
    if (!verifiedStaffUser?.id) {
      window.location.href = resolveAppUrl("signin.html");
      return;
    }
  }
  
  if (itemGrid) {
    await loadBrowseListings();
  }

  if (claimedGrid) {
    await loadClaimedListings();
  }
}

initializePage();

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
