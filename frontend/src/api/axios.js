import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:3000/api'
})

// Attach token if present.
//
// CORRECTION : ce code lisait jusqu'ici localStorage['auth'] (un objet
// JSON {token}), alors que VueConnexion.vue stocke le token directement
// sous la clé 'token' (localStorage.setItem('token', data.token)).
// Résultat : l'en-tête Authorization n'était en réalité jamais envoyé.
// Cela ne se voyait pas tant que le backend acceptait toutes les
// routes sans authentification (SecurityFilterConfig.permitAll()) ;
// maintenant que les routes protégées exigent un Bearer Token valide,
// ce mésalignement doit être corrigé pour que l'app continue de
// fonctionner après connexion.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default api
