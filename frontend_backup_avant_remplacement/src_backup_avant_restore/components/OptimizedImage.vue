<template>
  <picture>
    <source :srcset="webpSrc" type="image/webp" />
    <img
      :src="fallbackSrc"
      :alt="alt"
      :class="imgClass"
      :loading="lazy ? 'lazy' : 'eager'"
      :width="width"
      :height="height"
      v-bind="$attrs"
      @error="onError"
    />
  </picture>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  src: { type: String, required: true },
  alt: { type: String, default: '' },
  webp: { type: String, default: '' },
  fallback: { type: String, default: '' },
  lazy: { type: Boolean, default: true },
  imgClass: { type: String, default: '' },
  width: { type: [Number, String], default: null },
  height: { type: [Number, String], default: null },
})

const emit = defineEmits(['error'])

const webpSrc = computed(() => {
  if (props.webp) return props.webp
  return props.src.replace(/\.(png|jpg|jpeg)$/i, '.webp')
})

const fallbackSrc = computed(() => {
  if (props.fallback) return props.fallback
  return props.src
})

function onError(e) {
  if (e.target.src === fallbackSrc.value) {
    emit('error', e)
    return
  }
  e.target.src = fallbackSrc.value
}
</script>
