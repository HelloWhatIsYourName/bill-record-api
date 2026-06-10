<script setup lang="ts">
defineProps<{
  name: string
  size?: number
}>()
</script>

<template>
  <span class="material-symbols-outlined app-icon" :style="{ fontSize: `${size ?? 20}px` }" aria-hidden="true">
    {{ name }}
  </span>
</template>

