const TOKEN_KEY = 'ims.auth.token'
const USER_KEY = 'ims.auth.user'

function safeParse(value) {
    if (!value) return null

    try {
        return JSON.parse(value)
    } catch {
        return null
    }
}

export function getStoredToken() {
    return localStorage.getItem(TOKEN_KEY)
}

export function getStoredUser() {
    return safeParse(localStorage.getItem(USER_KEY))
}

export function persistSession(token, user) {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearSession() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
}
