<script setup lang="ts">
import AppIcon from './AppIcon.vue'
import { usePreferencesStore } from '@/stores/preferences'

const preferences = usePreferencesStore()

defineProps<{
  title: string
}>()

defineEmits<{
  close: []
}>()
</script>

<template>
  <div class="modal-backdrop" @click.self="$emit('close')">
    <section class="modal-panel" role="dialog" aria-modal="true" :aria-label="title">
      <header class="modal-panel__header">
        <h2>{{ title }}</h2>
        <button class="icon-button" type="button" @click="$emit('close')" :aria-label="preferences.t('common.close')">
          <AppIcon name="close" />
        </button>
      </header>
      <slot />
    </section>
  </div>
</template>
