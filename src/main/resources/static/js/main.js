const navToggleButton = document.getElementById("nav-toggle-btn");

navToggleButton?.addEventListener("click", () => {
  document.body.classList.toggle("nav-open");
});

const staffSignInForm =
  document.getElementById("staff-signin-form") ??
  document.querySelector(".staff-signin-form");
const signInStatus = document.getElementById("signin-status");

function updateSignInStatus(message, variant) {
  if (!signInStatus) {
    return;
  }

  signInStatus.hidden = false;
  signInStatus.textContent = message;
  signInStatus.className = `status-banner ${variant}`;
}

staffSignInForm?.addEventListener("submit", async (event) => {
  event.preventDefault();

  const formData = new FormData(staffSignInForm);
  const username = String(formData.get("username") ?? "").trim();
  const password = String(formData.get("password") ?? "");

  updateSignInStatus("Checking your credentials...", "is-success");

  try {
    const response = await fetch("/api/admins/signin", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ username, password }),
    });

    const payload = await response.json().catch(() => ({}));

    if (!response.ok) {
      throw new Error(payload.message || "Sign in failed");
    }

    sessionStorage.setItem("foundItStaffUser", JSON.stringify(payload));
    window.location.href = "/admin.html";
  } catch (error) {
    updateSignInStatus(
      error.message || "Unable to sign in right now.",
      "is-error",
    );
  }
});
