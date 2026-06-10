<script setup lang="ts">
import { computed } from 'vue'

import { formatPercent } from '@/utils/format'

const props = defineProps<{
  label: string
  value: number | string
  max: number | string
}>()

const percentage = computed(() => {
  const value = Number(props.value)
  const max = Number(props.max)
  if (!Number.isFinite(value) || !Number.isFinite(max) || max <= 0) {
    return 0
  }
  return Math.min(100, Math.max(0, (value / max) * 100))
})

const exactPercentage = computed(() => formatPercent(props.value, props.max))
</script>

<template>
  <div class="progress-bar">
    <div class="progress-bar__header">
      <span>{{ label }}</span>
      <span>{{ exactPercentage }}</span>
    </div>
    <div class="progress-bar__track" role="progressbar" :aria-label="label" :aria-valuenow="percentage" aria-valuemin="0" aria-valuemax="100">
      <div class="progress-bar__fill" :style="{ width: `${percentage}%` }" />
    </div>
  </div>
</template>

