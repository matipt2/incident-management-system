import { computed, ref } from 'vue'
import { fetchMe, login, register } from '../services/authApi'
import { clearSession, getStoredToken, getStoredUser, persistSession } from '../services/session'

const token = ref(getStoredToken())
const user = ref(getStoredUser())
const isBootstrapped = ref(false)

function setSession(authToken, authUser) {
    token.value = authToken
    user.value = authUser
    persistSession(authToken, authUser)
}

function removeSession() {
    token.value = null
    user.value = null
    clearSession()
}

async function bootstrapAuth() {
    if (isBootstrapped.value) return

    isBootstrapped.value = true
    if (!token.value) return

    try {
        const profile = await fetchMe()
        user.value = profile
        persistSession(token.value, profile)
    } catch {
        removeSession()
    }
}

async function loginUser(credentials) {
    const response = await login(credentials)
    const profile = {
        userId: response.userId,
        username: response.username,
        role: response.role,
        permissions: response.permissions || []
    }

    setSession(response.token, profile)
    return profile
}

async function registerUser(payload) {
    await register(payload)
}

function logoutUser() {
    removeSession()
}

export function useAuthStore() {
    return {
        token,
        user,
        isBootstrapped,
        isAuthenticated: computed(() => Boolean(token.value)),
        bootstrapAuth,
        loginUser,
        registerUser,
        logoutUser
    }
}
