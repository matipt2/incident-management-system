import { createRouter, createWebHistory } from 'vue-router'
import IncidentFormView from '../views/IncidentFormView.vue'
import IncidentSubmittedView from '../views/IncidentSubmittedView.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', name: 'incident-form', component: IncidentFormView },
        { path: '/submitted', name: 'incident-submitted', component: IncidentSubmittedView }
    ]
})

export default router