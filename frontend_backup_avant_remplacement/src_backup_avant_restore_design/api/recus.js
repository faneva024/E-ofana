import api from "./axios";

export const telechargerRecuPdf = async (idInscription) => {
  const response = await api.get(`/recus/${idInscription}/pdf`, {
    responseType: "blob",
  });

  const blob = new Blob([response.data], {
    type: "application/pdf",
  });

  const url = window.URL.createObjectURL(blob);
  const lien = document.createElement("a");

  lien.href = url;
  lien.download = `recu-inscription-${idInscription}.pdf`;

  document.body.appendChild(lien);
  lien.click();

  document.body.removeChild(lien);
  window.URL.revokeObjectURL(url);
};
