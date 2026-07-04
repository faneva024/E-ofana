<template>
  <div class="course-card">

    <div class="course-image">

      <img
        v-if="formation.image"
        :src="formation.image"
        :alt="formation.titre"
        class="course-image-img"
      />

      <div v-else class="image-placeholder">
        <i class="bi bi-image"></i>
        <span>Image Formation</span>
      </div>

      <div class="course-category">
        {{ formation.categorie || "Non défini" }}
      </div>

    </div>

    <div class="course-content">

      <h4 class="course-title">
        {{ formation.titre }}
      </h4>

      <p class="course-description">
        {{ formation.description }}
      </p>

      <div class="course-meta">

        <span>
          <i class="bi bi-clock me-1"></i>
          {{ formation.duree }}
        </span>

        <span>
          <i class="bi bi-play-circle me-1"></i>
          {{ formation.lecons }} leçons
        </span>

      </div>

      <div
        v-if="formation.progression !== undefined"
        class="course-progress"
      >
        <div class="progress" style="height: 8px;">
            <div
            class="progress-bar"
            :class="{ 'progress-bar-success': formation.progression === 100 }"
            :style="{ width: formation.progression + '%' }"
            ></div>
        </div>

        <small>{{ formation.progression }}% complété</small>
      </div>

      <button
        class="btn btn-primary-carte w-100 mt-3"
        @click="$emit('continue', formation)"
      >
        {{ formation.actionLabel || "Continuer" }}
      </button>

    </div>

  </div>
</template>

<script setup>
defineProps({
  formation: {
    type: Object,
    required: true
  }
})

defineEmits(['continue'])
</script>

<style scoped>
.course-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  background: #ffffff;
  transition: 0.2s;
}

.course-card:hover {
  transform: translateY(-4px);
}

.course-image {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.course-image-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  background:
    repeating-linear-gradient(
      45deg,
      #ececec,
      #ececec 12px,
      #dcdcdc 12px,
      #dcdcdc 24px
    );

  color: #666;
}

.image-placeholder i {
  font-size: 2.5rem;
  margin-bottom: 8px;
}

.image-placeholder span {
  font-size: 0.9rem;
  font-weight: 600;
}

.course-category {
  position: absolute;
  top: 10px;
  right: 10px;

  padding: 4px 12px;
  border-radius: 999px;

  background: rgba(255, 255, 255, 0.95);

  font-size: 0.8rem;
  font-weight: 600;
}

.course-content {
  padding: 16px;
}

.course-title {
  margin-bottom: 8px;
  font-size: 1.1rem;
  font-weight: 600;
}

.course-description {
  margin-bottom: 12px;
  color: #666;
  font-size: 0.9rem;
}

.course-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 0.85rem;
  color: #777;
}

.course-progress {
  margin-bottom: 12px;
}

.course-progress small {
  display: block;
  margin-top: 6px;
  color: #666;
}

@media (max-width: 768px) {
  .course-image {
    height: 160px;
  }

  .course-meta {
    flex-direction: column;
    gap: 6px;
  }
}
</style>