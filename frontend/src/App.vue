<template>
  <router-view v-if="pageSansLayout" />

  <LayoutPrincipal v-else>
    <router-view />
  </LayoutPrincipal>
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import LayoutPrincipal from "./layouts/LayoutPrincipal.vue";

const route = useRoute();

const pageSansLayout = computed(() => {
  return (
    route.path.startsWith("/formateur") ||
    route.path.startsWith("/admin")
  );
});
</script>

<style>
html,
body,
#app {
  min-height: 100vh;
  margin: 0;
}
</style>