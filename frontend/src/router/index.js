import { createRouter, createWebHistory } from 'vue-router'
import IncidentFormView from '../views/IncidentFormView.vue'
import IncidentSubmittedView from '../views/IncidentSubmittedView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', name: 'incident-form', component: IncidentFormView, meta: { requiresAuth: true } },
        { path: '/submitted', name: 'incident-submitted', component: IncidentSubmittedView, meta: { requiresAuth: true } },
        { path: '/login', name: 'login', component: LoginView, meta: { guestOnly: true } },
        { path: '/register', name: 'register', component: RegisterView, meta: { guestOnly: true } }
    ]
})

router.beforeEach(async (to) => {
    const auth = useAuthStore()
    await auth.bootstrapAuth()

    if (to.meta.requiresAuth && !auth.isAuthenticated.value) {
        return {
            name: 'login',
            query: { redirect: to.fullPath }
        }
    }

    if (to.meta.guestOnly && auth.isAuthenticated.value) {
        return { name: 'incident-form' }
    }

    return true
})

export default router