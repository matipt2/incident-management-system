<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

const props = defineProps({
  role: {
    type: String,
    default: ''
  }
})

const route = useRoute()

const links = computed(() => {
  if (props.role === 'REPORTER') {
    return [
      { to: '/report', label: 'Report incident' },
      { to: '/incidents', label: 'My incidents' }
    ]
  }

  if (props.role === 'AGENT') {
    return [
      { to: '/incidents', label: 'Assigned incidents' }
    ]
  }

  return [
    { to: '/report', label: 'Report incident' },
    { to: '/incidents', label: 'All incidents' }
  ]
})

function isActive(path) {
  return route.path === path || (path === '/incidents' && route.path.startsWith('/incidents/'))
}
</script>

<template>
  <aside class="sidebar">
    <p class="sidebar__title">Navigation</p>
    <nav class="sidebar__nav">
      <RouterLink
        v-for="item in links"
        :key="item.to"
        :to="item.to"
        class="sidebar__link"
        :class="{ 'sidebar__link--active': isActive(item.to) }"
      >
        {{ item.label }}
      </RouterLink>
    </nav>
  </aside>
</template>
