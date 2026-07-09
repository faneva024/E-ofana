import api from "./axios";

const normaliserFormation = (formation) => {
  const id = formation.idFormation || formation.id || formation._id;

  const prix = Number(formation.prix || formation.price || 0);
  const prixRemise = Number(formation.prixRemise || 0);
  const prixFinal = prixRemise > 0 ? prixRemise : prix;

  const nombrePlaces =
    Number(
      formation.nombrePlaces ??
        formation.placesRestantes ??
        formation.placesDisponibles ??
        0
    ) || 0;

  return {
    ...formation,

    id,
    idFormation: id,

    titre: formation.titre || formation.title || "Formation sans titre",
    title: formation.titre || formation.title || "Formation sans titre",

    description: formation.description || "",

    categorie:
      formation.categorie ||
      formation.category ||
      formation.nomCategorie ||
      "Formation",

    category:
      formation.categorie ||
      formation.category ||
      formation.nomCategorie ||
      "Formation",

    centre:
      formation.centre ||
      formation.nomCentre ||
      formation.school ||
      "Centre non défini",

    ville:
      formation.ville ||
      formation.lieu ||
      formation.location ||
      "Antananarivo",

    lieu:
      formation.lieu ||
      formation.ville ||
      formation.location ||
      "Antananarivo",

    duree: formation.duree || formation.duration || "Durée non définie",

    prix,
    prixRemise,
    prixFinal,

    lecons: formation.lecons || formation.nbLecons || 0,

    nombrePlaces,
    placesRestantes: nombrePlaces,
    placesDisponibles: nombrePlaces > 0,

    noteMoyenne: formation.noteMoyenne || 0,
    nbAvis: formation.nbAvis || 0,

    image: formation.image || "",
  };
};

const normaliserListe = (data) => {
  if (Array.isArray(data)) {
    return data.map(normaliserFormation);
  }

  if (data && Array.isArray(data.content)) {
    return data.content.map(normaliserFormation);
  }

  if (data && Array.isArray(data.formations)) {
    return data.formations.map(normaliserFormation);
  }

  if (data && Array.isArray(data.results)) {
    return data.results.map(normaliserFormation);
  }

  return [];
};

export const obtenirFormations = async (params = {}) => {
  const response = await api.get("/formations", { params });
  return normaliserListe(response.data);
};

export const getFormations = (params = {}) => {
  return api.get("/formations", { params });
};

export const obtenirFormationParId = async (id) => {
  const response = await api.get(`/formations/${id}`);
  return normaliserFormation(response.data);
};

export const getFormationById = (id) => {
  return api.get(`/formations/${id}`);
};

export const creerFormation = (data) => {
  return api.post("/formations", data);
};

export const createFormation = (data) => {
  return api.post("/formations", data);
};

export const modifierFormation = (id, data) => {
  return api.put(`/formations/${id}`, data);
};

export const supprimerFormation = (id) => {
  return api.delete(`/formations/${id}`);
};