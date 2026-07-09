<template>
  <footer class="footer-custom text-center">
    <div class="container">
      <div class="mb-2">
        <span class="footer-brand-text">E-OFANA</span> 
        <span class="text-white-50">— Plateforme de formations professionnelles à Madagascar</span>
      </div>
      <div class="footer-links d-flex justify-content-center gap-4 mb-3">
        <a href="#">À propos</a>
        <a href="#">Contact</a>
        <a href="#">Conditions d'utilisation</a>
        <a href="#">Politique de confidentialité</a>
      </div>
      <div class="footer-copyright">
        © 2026 E-OFANA. Tous droits réservés.
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer-custom {
  background-color: #121212;
  color: #ffffff;
  padding: 4rem 0;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.footer-brand-text {
  color: #c59d5f;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.2s;
} 

.footer-links a:hover {
  color: #ffffff;
}

.footer-copyright {
  color: rgba(255, 255, 255, 0.3);
  font-size: 0.85rem;
}
</style>