describe('incident workflows', () => {
  beforeEach(() => {
    cy.clearLocalStorage()
    cy.loginAsReporter()
  })

  it('lists incidents returned by the API', () => {
    cy.intercept('GET', '/api/my/incidents*', [
      {
        id: 'INC-1001',
        title: 'Payment API outage',
        status: 'NEW',
        priority: 'HIGH',
        category: 'APPLICATION',
        assignedTo: null,
        updatedAt: '2026-05-13T20:00:00Z'
      }
    ]).as('listIncidents')

    cy.visit('/incidents')

    cy.wait('@fetchMe')
    cy.wait('@listIncidents')
    cy.contains('Payment API outage').should('be.visible')
    cy.contains('INC-1001').should('be.visible')
  })

  it('submits an incident and shows confirmation', () => {
    cy.intercept('POST', '/api/incidents', (req) => {
      expect(req.body).to.include({
        title: 'Cannot access admin panel',
        description: 'Users receive a 503 response.',
        channel: 'FORM',
        projectId: 'OPS'
      })

      req.reply({
        statusCode: 201,
        body: {
          id: 'INC-2002',
          title: req.body.title,
          status: 'NEW'
        }
      })
    }).as('createIncident')

    cy.visit('/report')

    cy.wait('@fetchMe')
    cy.get('#title').type('Cannot access admin panel')
    cy.get('#description').type('Users receive a 503 response.')
    cy.get('#projectId').type('OPS')
    cy.contains('button', 'Create incident').click()

    cy.wait('@createIncident')
    cy.location('pathname').should('eq', '/submitted')
    cy.contains('INC-2002').should('be.visible')
    cy.contains('Cannot access admin panel').should('be.visible')
    cy.contains('NEW').should('be.visible')
  })
})
