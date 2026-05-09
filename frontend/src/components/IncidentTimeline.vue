<script setup>
defineProps({
  events: {
    type: Array,
    default: () => []
  }
})

function formatDate(value) {
  if (!value) return 'N/A'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? 'N/A' : date.toLocaleString()
}
</script>

<template>
  <section class="timeline-card">
    <h3 class="section-title">Incident history</h3>

    <p v-if="!events.length" class="empty-note">No timeline entries yet.</p>

    <ol v-else class="timeline">
      <li v-for="(event, index) in events" :key="`${event.occurredAt}-${index}`" class="timeline__item">
        <div class="timeline__dot" />
        <div class="timeline__content">
          <div class="timeline__header">
            <span class="event-badge">{{ event.eventType }}</span>
            <time class="timeline__time">{{ formatDate(event.occurredAt) }}</time>
          </div>
          <p class="timeline__details">{{ event.details || 'No details' }}</p>
          <p class="timeline__meta">By: {{ event.performedBy || 'system' }}</p>
        </div>
      </li>
    </ol>
  </section>
</template>
