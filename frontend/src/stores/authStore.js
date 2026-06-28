import { defineStore } from 'pinia'
import router from '../router'
import axios from 'axios'

const API_BASE = '/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null,
    loading: false,
    error: null
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    currentUser: (state) => state.user
  },
  actions: {
    setUser(user, token) {
      this.user = user
      this.token = token
      localStorage.setItem('auth', JSON.stringify({ user, token }))
      
      // Set axios default header
      if (token) {
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      }
    },
    loadFromStorage() {
      const raw = localStorage.getItem('auth')
      if (raw) {
        try {
          const { user, token } = JSON.parse(raw)
          this.user = user
          this.token = token
          
          // Set axios default header
          if (token) {
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
          }
        } catch (e) {
          this.user = null
          this.token = null
          localStorage.removeItem('auth')
        }
      }
    },
    logout() {
      this.user = null
      this.token = null
      this.error = null
      localStorage.removeItem('auth')
      delete axios.defaults.headers.common['Authorization']
      router.push({ name: 'Home' })
    },
    async login(email, password) {
      this.loading = true
      this.error = null
      try {
        const response = await axios.post(`${API_BASE}/auth/login`, {
          email,
          password
        })
        
        const { user, token } = response.data
        this.setUser(user, token)
        router.push({ name: 'MonEspace' })
        
        return { success: true }
      } catch (error) {
        this.error = error.response?.data?.message || 'Erreur de connexion'
        return { success: false, error: this.error }
      } finally {
        this.loading = false
      }
    },
    async register(userData) {
      this.loading = true
      this.error = null
      try {
        const response = await axios.post(`${API_BASE}/auth/register`, userData)
        
        const { user, token } = response.data
        this.setUser(user, token)
        router.push({ name: 'MonEspace' })
        
        return { success: true }
      } catch (error) {
        this.error = error.response?.data?.message || 'Erreur lors de l\'inscription'
        return { success: false, error: this.error }
      } finally {
        this.loading = false
      }
    }
  }
})
