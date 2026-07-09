import api from "./api";

const normaliserFormation = (formation) => {
  const id = formation.idFormation || formation.id || formation._id;

  const prix = Number(formation.prix || 0);
  const prixRemise = Number(formation.prixRemise || 0);

  return {
    ...formation,

    id,
    idFormation: id,

    titre: formation.titre || formation.title || "Formation sans titre",
    title: formation.titre || formation.title || "Formation sans titre",

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

    dateDebut: formation.dateDebut || "À définir",
    dateLimiteInscription: formation.dateLimiteInscription || "À définir",

    prix,
    prixRemise,
    prixFinal: prixRemise > 0 ? prixRemise : prix,

    placesRestantes:
      Number(
        formation.placesRestantes ||
          formation.nombrePlaces ||
          formation.placesDisponibles ||
          10
      ) || 10,

    nombrePlaces:
      Number(
        formation.nombrePlaces ||
          formation.placesRestantes ||
          formation.placesDisponibles ||
          10
      ) || 10,

    placesDisponibles: true,

    noteMoyenne: formation.noteMoyenne || 0,
    nbAvis: formation.nbAvis || 0,

    image: formation.image || "",
  };
};

const normaliserListe = (data) => {
  if (Array.isArray(data)) return data.map(normaliserFormation);
  if (data && Array.isArray(data.content)) return data.content.map(normaliserFormation);
  if (data && Array.isArray(data.formations)) return data.formations.map(normaliserFormation);
  if (data && Array.isArray(data.results)) return data.results.map(normaliserFormation);
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