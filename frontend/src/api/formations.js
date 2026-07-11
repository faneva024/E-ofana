const API_BASE_URL = "http://localhost:8081/api/v1";

const normaliserFormation = (formation) => {
  const prix = Number(formation.prix || 0);
  const prixRemise = Number(formation.prixRemise || 0);
  const prixFinal = prixRemise > 0 ? prixRemise : prix;

  return {
    ...formation,

    id: formation.idFormation,
    idFormation: formation.idFormation,

    title: formation.titre || "Formation sans titre",
    titre: formation.titre || "Formation sans titre",

    description: formation.description || "",

    category: formation.categorie || "Formation",
    categorie: formation.categorie || "Formation",

    centre: formation.centre || "Centre non défini",

    ville: formation.ville || formation.lieu || "Antananarivo",
    lieu: formation.lieu || formation.ville || "Antananarivo",

    duree: formation.duree || "Durée non définie",
    dateDebut: formation.dateDebut || "À définir",

    prix: prixFinal,
    prixOriginal: prix,
    prixRemise,

    placesDisponibles: true,
    placesRestantes: formation.placesRestantes || 20,

    pertinence: 100,
  };
};

export const obtenirFormations = async () => {
  console.log("APPEL API FORMATIONS =", `${API_BASE_URL}/formations`);

  const response = await fetch(`${API_BASE_URL}/formations`);

  if (!response.ok) {
    throw new Error("Impossible de charger les formations");
  }

  const data = await response.json();

  console.log("FORMATIONS API =", data);

  if (Array.isArray(data)) {
    return data.map(normaliserFormation);
  }

  return [];
};

export const obtenirFormationParId = async (id) => {
  const response = await fetch(`${API_BASE_URL}/formations/${id}`);

  if (!response.ok) {
    throw new Error("Impossible de charger la formation");
  }

  const data = await response.json();

  return normaliserFormation(data);
};

export const getFormations = async () => {
  const data = await obtenirFormations();
  return { data };
};

export const getFormationById = async (id) => {
  const data = await obtenirFormationParId(id);
  return { data };
};