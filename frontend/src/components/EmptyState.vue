<script setup lang="ts">
import AppIcon from './AppIcon.vue'

defineProps<{
  icon: string
  title: string
  text: string
}>()
</script>

<template>
  <div class="empty-state">
    <AppIcon :name="icon" :size="28" />
    <strong>{{ title }}</strong>
    <span>{{ text }}</span>
  </div>
</template>

