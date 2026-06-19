const manager = {
  userId: 'manager-1',
  username: 'manager',
  role: 'MANAGER',
  permissions: [
    'INCIDENT_READ', 'INCIDENT_REPORT', 'INCIDENT_ASSIGN', 'INCIDENT_CLOSE',
    'PROJECT_READ', 'PROJECT_WRITE', 'USER_MANAGE'
  ]
}

describe('manager administration', () => {
  beforeEach(() => {
    cy.clearLocalStorage()
    cy.intercept('GET', '/api/me/resources', manager).as('fetchMe')
  })

  function visitAsManager(path) {
    cy.visit(path, {
      onBeforeLoad(win) {
        win.localStorage.setItem('ims.auth.token', 'manager-token')
        win.localStorage.setItem('ims.auth.user', JSON.stringify(manager))
      }
    })
  }

  it('creates a project', () => {
    cy.intercept('GET', '/api/projects?includeInactive=true', []).as('projects')
    cy.intercept('POST', '/api/projects', {
      statusCode: 201,
      body: { key: 'PAYMENTS', name: 'Payments', active: true }
    }).as('createProject')

    visitAsManager('/projects')
    cy.wait('@fetchMe')
    cy.wait('@projects')
    cy.get('input[placeholder="PROJECT-KEY"]').type('PAYMENTS')
    cy.get('input[placeholder="Project name"]').type('Payments')
    cy.contains('button', 'Save').click()
    cy.wait('@createProject')
  })

  it('changes a user role', () => {
    cy.intercept('GET', '/api/management/users', [
      { userId: 'user-2', username: 'support', email: 'support@example.com', role: 'REPORTER' }
    ]).as('users')
    cy.intercept('PATCH', '/api/management/users/user-2/role', {
      userId: 'user-2', username: 'support', email: 'support@example.com', role: 'AGENT'
    }).as('updateRole')

    visitAsManager('/users')
    cy.wait('@fetchMe')
    cy.wait('@users')
    cy.get('select').select('AGENT')
    cy.wait('@updateRole')
  })
})
