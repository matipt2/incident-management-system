const reporter = {
  userId: 'user-1',
  username: 'reporter',
  role: 'REPORTER',
  permissions: ['INCIDENT_REPORT', 'INCIDENT_READ']
}

Cypress.Commands.add('loginAsReporter', () => {
  window.localStorage.setItem('ims.auth.token', 'test-token')
  window.localStorage.setItem('ims.auth.user', JSON.stringify(reporter))

  cy.intercept('GET', '/api/me/resources', reporter).as('fetchMe')
})
