import { defineStore } from 'pinia'
import axios from 'axios'

const API_BASE = '/api/v1/auth-formateur'

export const useAuthFormateurStore = defineStore('authFormateur', {
  state: () => ({
    formateur: null,
    token: null,
    loading: false,
    error: null
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    currentFormateur: (state) => state.formateur
  },
  actions: {
    setFormateur(formateur, token) {
      this.formateur = formateur
      this.token = token
      localStorage.setItem('authFormateur', JSON.stringify({ formateur, token }))

      if (token) {
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      }
    },
    loadFromStorage() {
      const raw = localStorage.getItem('authFormateur')
      if (raw) {
        try {
          const { formateur, token } = JSON.parse(raw)
          this.formateur = formateur
          this.token = token

          if (token) {
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
          }
        } catch (e) {
          this.formateur = null
          this.token = null
          localStorage.removeItem('authFormateur')
        }
      }
    },
    logout() {
      this.formateur = null
      this.token = null
      this.error = null
      localStorage.removeItem('authFormateur')
      delete axios.defaults.headers.common['Authorization']
    },
    async login(email, password) {
      this.loading = true
      this.error = null
      try {
        const response = await axios.post(`${API_BASE}/connexion`, {
          email,
          password
        })

        const { formateur, token } = response.data
        this.setFormateur(formateur, token)

        return { success: true }
      } catch (error) {
        const status = error.response?.status
        const message = error.response?.data?.message

        if (status === 403) {
          this.error = message || 'Votre compte est suspendu. Veuillez contacter votre administrateur.'
        } else if (status === 401) {
          this.error = message || 'Email ou mot de passe incorrect.'
        } else {
          this.error = message || 'Erreur de connexion. Veuillez réessayer.'
        }

        return { success: false, error: this.error }
      } finally {
        this.loading = false
      }
    }
  }
})
