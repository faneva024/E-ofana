import api from "./api";

const normaliserFormation = (formation) => {
  const id = formation.idFormation || formation.id || formation._id;

  const prix = Number(formation.prix || 0);
  const prixRemise = Number(formation.prixRemise || 0);
  const prixFinal = prixRemise > 0 ? prixRemise : prix;

  const places =
    formation.placesRestantes ||
    formation.nombrePlaces ||
    formation.placesDisponibles ||
    0;

  return {
    ...formation,

    id,
    idFormation: id,

    titre: formation.titre || formation.nom || "Formation sans titre",
    title: formation.titre || formation.nom || "Formation sans titre",

    description: formation.description || "",

    categorie:
      formation.categorie ||
      formation.nomCategorie ||
      formation.category ||
      "Formation",

    category:
      formation.categorie ||
      formation.nomCategorie ||
      formation.category ||
      "Formation",

    centre:
      formation.centre ||
      formation.nomCentre ||
      formation.centreNom ||
      "Centre non défini",

    ville: formation.ville || formation.lieu || "",
    lieu: formation.lieu || formation.ville || "Lieu non défini",

    duree: formation.duree || "Durée non définie",

    prix,
    prixRemise,
    prixFinal,

    lecons: formation.lecons || formation.nbLecons || 0,

    nombrePlaces: Number(places),
    placesRestantes: Number(places),
    placesDisponibles: Number(places) > 0,

    noteMoyenne: formation.noteMoyenne || 0,
    nbAvis: formation.nbAvis || 0,

    image: formation.image || "",
    commencee: false,
    progression: 0,
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

export const getFormationById = (id) => {
  return api.get(`/formations/${id}`);
};

export const obtenirFormationParId = async (id) => {
  const response = await api.get(`/formations/${id}`);
  return normaliserFormation(response.data);
};

export const createFormation = (data) => {
  return api.post("/formations", data);
};

export const creerFormation = (data) => {
  return api.post("/formations", data);
};

export const modifierFormation = (id, data) => {
  return api.put(`/formations/${id}`, data);
};

export const supprimerFormation = (id) => {
  return api.delete(`/formations/${id}`);
};

export const obtenirCategories = () => {
  return api.get("/formations/categories");
};