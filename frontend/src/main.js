import { createApp } from "vue";

const App = {
  template: `
    <div style="font-family: Arial; padding: 30px;">
      <h1>E-OFANA</h1>
      <p>Frontend Vue.js temporaire démarré avec Docker.</p>
      <p>Backend URL : {{ apiUrl }}</p>
    </div>
  `,
  data() {
    return {
      apiUrl: import.meta.env.VITE_API_URL
    };
  }
};

createApp(App).mount("#app");
