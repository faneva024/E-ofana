import { createApp } from "vue";
import { createPinia } from "pinia";

import App from "./App.vue";
import router from "./router";

import OptimizedImage from "./components/OptimizedImage.vue";

import "./assets/scss/main.scss";
import "./styles/variables.css";

import { useAuthStore } from "./stores/authStore";
import { useAuthFormateurStore } from "./stores/authFormateurStore";

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);

app.component("OptimizedImage", OptimizedImage);

const auth = useAuthStore();
auth.loadFromStorage();

const authFormateur = useAuthFormateurStore();
authFormateur.loadFromStorage();

app.mount("#app");