import { defineStore } from 'pinia'
import axios from 'axios'

const API_BASE = '/api/v1/auth-admin'

export const useAuthAdminStore = defineStore('authAdmin', {
  state: () => ({
    admin: null,
    token: null,
    loading: false,
    error: null
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    currentAdmin: (state) => state.admin
  },
  actions: {
    setAdmin(admin, token) {
      this.admin = admin
      this.token = token
      localStorage.setItem('authAdmin', JSON.stringify({ admin, token }))

      if (token) {
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      }
    },
    loadFromStorage() {
      const raw = localStorage.getItem('authAdmin')
      if (raw) {
        try {
          const { admin, token } = JSON.parse(raw)
          this.admin = admin
          this.token = token

          if (token) {
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
          }
        } catch (e) {
          this.admin = null
          this.token = null
          localStorage.removeItem('authAdmin')
        }
      }
    },
    logout() {
      this.admin = null
      this.token = null
      this.error = null
      localStorage.removeItem('authAdmin')
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

        const { admin, token } = response.data
        this.setAdmin(admin, token)

        return { success: true }
      } catch (error) {
        const status = error.response?.status
        const message = error.response?.data?.message

        if (status === 403) {
          this.error = message || 'Votre compte est suspendu.'
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
