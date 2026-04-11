<script setup>
import { computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'

const route = useRoute()

const incidentId = computed(() => route.query.id || 'N/A')
const incidentTitle = computed(() => route.query.title || 'Untitled incident')
const incidentStatus = computed(() => route.query.status || 'Unknown')
</script>

<template>
  <section class="success-page">
    <div class="success-card">
      <div class="success-icon">✓</div>
      <p class="success-card__eyebrow">Incident created</p>
      <h2 class="success-card__title">Your incident has been submitted</h2>
      <p class="success-card__description">
        The report was created successfully and is now ready for further handling.
      </p>

      <div class="summary-grid">
        <div class="summary-item">
          <span class="summary-item__label">Incident ID</span>
          <strong class="summary-item__value">{{ incidentId }}</strong>
        </div>

        <div class="summary-item">
          <span class="summary-item__label">Status</span>
          <strong class="summary-item__value">{{ incidentStatus }}</strong>
        </div>

        <div class="summary-item summary-item--full">
          <span class="summary-item__label">Title</span>
          <strong class="summary-item__value">{{ incidentTitle }}</strong>
        </div>
      </div>

      <div class="success-actions">
        <RouterLink to="/" class="btn btn--primary">
          Report another incident
        </RouterLink>
      </div>
    </div>
  </section>
</template>