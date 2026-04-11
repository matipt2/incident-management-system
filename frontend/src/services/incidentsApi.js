const USE_MOCK = false

export async function createIncident(payload) {
    if (USE_MOCK) {
        return {
            id: `INC-${Math.floor(Math.random() * 9000 + 1000)}`,
            title: payload.title,
            status: 'NEW'
        }
    }

    const response = await fetch('/api/incidents', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })

    if (!response.ok) {
        throw new Error('Error while creating incident.')
    }

    return response.json()
}