const API_BASE_URL = "http://localhost:8081/api/v1";

export const getMesInscriptions = async (idUser) => {
  const response = await fetch(`${API_BASE_URL}/apprenants/${idUser}/inscriptions`);

  if (!response.ok) {
    throw new Error("Impossible de charger les inscriptions");
  }

  return await response.json();
};

export const getMesRecus = async (idUser) => {
  const response = await fetch(`${API_BASE_URL}/apprenants/${idUser}/recus`);

  if (!response.ok) {
    throw new Error("Impossible de charger les reçus");
  }

  return await response.json();
};

export const getMesStats = async (idUser) => {
  const response = await fetch(`${API_BASE_URL}/apprenants/${idUser}/stats`);

  if (!response.ok) {
    throw new Error("Impossible de charger les statistiques");
  }

  return await response.json();
};

export const telechargerRecuApprenant = async (idInscription) => {
  const response = await fetch(`${API_BASE_URL}/recus/${idInscription}/pdf`);

  if (!response.ok) {
    throw new Error("Impossible de télécharger le reçu PDF");
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);

  const link = document.createElement("a");
  link.href = url;
  link.download = `recu-inscription-${idInscription}.pdf`;

  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  window.URL.revokeObjectURL(url);
};