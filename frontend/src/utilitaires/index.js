import { useAuthStore } from "../stores/storeAuthentification";
import { mockFormations } from "../api/formations"

const BASE_URL = "/api";

export async function requeteAvecAuth(url, options = {}) {
//   const auth = useAuthStore();
//   const token = auth.obtenirToken();
    const auth = true;
    const token = true;

  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(BASE_URL + url, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const error = await response.json();
    throw error;
  }

  return await response.json();
}

export function formaterPrix(prix) {
  return new Intl.NumberFormat("fr-FR").format(prix) + " Ar";
}

export function formaterDate(date) {
  return new Date(date).toLocaleDateString("fr-FR", {
    day: "2-digit",
    month: "long",
    year: "numeric",
  });
}

export function afficherNotification(message, type = "success") {
  const div = document.createElement("div");

  div.className = `alert alert-${type} position-fixed top-0 end-0 m-3`;
  div.style.zIndex = 9999;
  div.innerText = message;

  document.body.appendChild(div);

  setTimeout(() => {
    div.remove();
  }, 3000);
}

