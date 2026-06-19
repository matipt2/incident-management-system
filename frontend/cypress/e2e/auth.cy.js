describe('authentication flow', () => {
  beforeEach(() => {
    cy.clearLocalStorage()
  })

  it('redirects guests from incidents to login and signs in', () => {
    cy.intercept('POST', '/api/auth/login', {
      token: 'jwt-token',
      userId: 'user-1',
      username: 'reporter',
      role: 'REPORTER',
      permissions: ['INCIDENT_REPORT', 'INCIDENT_READ', 'PROJECT_READ']
    }).as('login')

    cy.intercept('GET', '/api/me/resources', {
      userId: 'user-1',
      username: 'reporter',
      role: 'REPORTER',
      permissions: ['INCIDENT_REPORT', 'INCIDENT_READ', 'PROJECT_READ']
    }).as('fetchMe')
    cy.intercept('GET', '/api/my/incidents*', []).as('listIncidents')

    cy.visit('/incidents')

    cy.location('pathname').should('eq', '/login')
    cy.get('#username').type('reporter')
    cy.get('#password').type('password123')
    cy.contains('button', 'Sign in').click()

    cy.wait('@login')
    cy.location('pathname').should('eq', '/incidents')
    cy.contains('reporter (REPORTER)').should('be.visible')
  })

  it('registers a new user and navigates to login', () => {
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 201,
      body: {
        userId: 'user-2',
        username: 'newreporter',
        email: 'newreporter@example.com',
        role: 'REPORTER',
        permissions: ['INCIDENT_REPORT', 'INCIDENT_READ', 'PROJECT_READ']
      }
    }).as('register')

    cy.visit('/register')

    cy.get('#username').type('newreporter')
    cy.get('#email').type('newreporter@example.com')
    cy.get('#password').type('password123')
    cy.contains('button', 'Create account').click()

    cy.wait('@register')
    cy.contains('Account created. You can now sign in.').should('be.visible')
    cy.location('pathname', { timeout: 2000 }).should('eq', '/login')
  })
})
