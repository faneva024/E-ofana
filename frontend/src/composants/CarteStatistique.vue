<template>
  <div class="stat-card card border-0 shadow-sm h-100" :class="toneClass">
    <div class="card-body p-3 p-lg-4">
      <div class="d-flex align-items-start gap-3">
        <div class="stat-icon" :class="iconClass">
          <slot name="icon">
            <span aria-hidden="true">•</span>
          </slot>
        </div>

        <div class="flex-grow-1 min-w-0">
          <div class="stat-label">{{ label }}</div>
          <div v-if="loading" class="stat-skeleton"></div>
          <div v-else class="stat-value">{{ value }}</div>
          <div v-if="hint" class="stat-hint">{{ hint }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    default: '0'
  },
  hint: {
    type: String,
    default: ''
  },
  tone: {
    type: String,
    default: 'light'
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const toneClass = computed(() => `tone-${props.tone}`)

const iconClass = computed(() => {
  const map = {
    primary: 'bg-primary-subtle text-primary',
    success: 'bg-success-subtle text-success',
    warning: 'bg-warning-subtle text-warning',
    light: 'bg-light text-secondary',
    'primary-subtle': 'bg-primary-subtle text-primary'
  }

  return map[props.tone] || map.light
})
</script>

<style scoped>
.stat-card {
  border-radius: 1.25rem;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border: 1px solid #eef0f2 !important;
  background: #ffffff;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-label {
  color: #6c757d;
  font-size: 0.82rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  margin-bottom: 0.4rem;
}

.stat-value {
  color: #111827;
  font-size: clamp(1.4rem, 2vw, 2rem);
  font-weight: 800;
  line-height: 1.1;
}

.stat-hint {
  color: #8a94a6;
  font-size: 0.82rem;
  margin-top: 0.35rem;
}

.stat-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 0.9rem;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.tone-primary .stat-icon {
  background: rgba(26, 26, 26, 0.08) !important;
  color: #1a1a1a !important;
}

.tone-primary-subtle .stat-icon {
  background: rgba(13, 110, 253, 0.12) !important;
  color: #0d6efd !important;
}

.tone-success .stat-icon {
  background: rgba(46, 125, 50, 0.12) !important;
  color: #2e7d32 !important;
}

.tone-warning .stat-icon {
  background: rgba(245, 158, 11, 0.14) !important;
  color: #b7791f !important;
}

.tone-light .stat-icon {
  background: #f5f5f5 !important;
  color: #6c757d !important;
}

.stat-icon :deep(svg) {
  width: 1.15rem;
  height: 1.15rem;
}

.stat-skeleton {
  width: min(180px, 70%);
  height: 1.65rem;
  border-radius: 999px;
  background: linear-gradient(90deg, #e9ecef 0%, #f8f9fa 50%, #e9ecef 100%);
  background-size: 200% 100%;
  animation: shimmer 1.2s linear infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
}
</style>