<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import { computed } from 'vue'

import AppShell from '@/components/AppShell.vue'

const route = useRoute()
const publicLayout = computed(() => route.meta.public === true)
</script>

<template>
  <RouterView v-if="publicLayout" />
  <AppShell v-else />
</template>

