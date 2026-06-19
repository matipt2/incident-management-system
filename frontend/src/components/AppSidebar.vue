<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

const props = defineProps({
  permissions: {
    type: Array,
    default: () => []
  }
})

const route = useRoute()

const links = computed(() => {
  const permissions = new Set(props.permissions)
  const items = [{ to: '/incidents', label: 'Incidents' }]
  if (permissions.has('INCIDENT_REPORT')) items.unshift({ to: '/report', label: 'Report incident' })
  if (permissions.has('SLA_READ')) items.push({ to: '/sla', label: 'SLA' })
  if (permissions.has('PROJECT_WRITE')) items.push({ to: '/projects', label: 'Projects' })
  if (permissions.has('USER_MANAGE')) items.push({ to: '/users', label: 'User roles' })
  return items
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
