import api from "../api/axios";

export async function requeteAvecAuth(url, options = {}) {
  const response = await api({
    url,
    method: options.method || "GET",
    data: options.body,
    params: options.params,
    headers: options.headers,
  });

  return response.data;
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

  setTimeout(() => div.remove(), 3000);
}
