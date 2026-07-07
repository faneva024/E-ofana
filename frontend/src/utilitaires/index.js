const API_BASE_URL =
  import.meta.env.VITE_API_URL || "http://localhost:8081/api/v1";


const nettoyerUrl = (url) => {
  return url.replace(/([^:]\/)\/+/g, "$1");
};


export const getApiUrl = (url) => {
  if (!url) {
    return API_BASE_URL;
  }

  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }

  const apiOrigin = API_BASE_URL.replace(/\/api\/v1\/?$/, "");

  if (url.startsWith("/api/")) {
    return nettoyerUrl(`${apiOrigin}${url}`);
  }

  const cleanUrl = url.startsWith("/") ? url : `/${url}`;

  return nettoyerUrl(`${API_BASE_URL}${cleanUrl}`);
};


export const getToken = () => {
  return localStorage.getItem("token");
};


export const setToken = (token) => {
  if (token) {
    localStorage.setItem("token", token);
  }
};


export const removeToken = () => {
  localStorage.removeItem("token");
};


export const getUtilisateur = () => {
  const utilisateur = localStorage.getItem("utilisateur");

  if (!utilisateur) {
    return null;
  }

  try {
    return JSON.parse(utilisateur);
  } catch (error) {
    console.error("Erreur lecture utilisateur :", error);
    return null;
  }
};


export const setUtilisateur = (utilisateur) => {
  if (utilisateur) {
    localStorage.setItem("utilisateur", JSON.stringify(utilisateur));
  }
};


export const removeUtilisateur = () => {
  localStorage.removeItem("utilisateur");
};

export const estConnecte = () => {
  return !!getToken();
};


export const deconnexion = () => {
  removeToken();
  removeUtilisateur();
  localStorage.removeItem("user");
};


export const requeteAvecAuth = async (url, options = {}) => {
  const token = getToken();

  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return fetch(getApiUrl(url), {
    ...options,
    headers,
  });
};


export const getAvecAuth = async (url) => {
  return requeteAvecAuth(url, {
    method: "GET",
  });
};


export const postAvecAuth = async (url, data) => {
  return requeteAvecAuth(url, {
    method: "POST",
    body: JSON.stringify(data),
  });
};


export const putAvecAuth = async (url, data) => {
  return requeteAvecAuth(url, {
    method: "PUT",
    body: JSON.stringify(data),
  });
};


export const deleteAvecAuth = async (url) => {
  return requeteAvecAuth(url, {
    method: "DELETE",
  });
};


export const formatPrix = (value) => {
  if (!value) {
    return "0 Ar";
  }

  return `${new Intl.NumberFormat("fr-FR").format(value)} Ar`;
};


export const formatDate = (date) => {
  if (!date) {
    return "";
  }

  return new Intl.DateTimeFormat("fr-FR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(date));
};