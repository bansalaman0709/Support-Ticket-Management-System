import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { CreateTicketPage } from '../pages/CreateTicketPage'
import * as ticketsApi from '../api/tickets'
import { ApiError } from '../types'

vi.mock('../api/tickets')

describe('CreateTicketPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('shows client validation when title is blank', async () => {
    render(
      <MemoryRouter>
        <CreateTicketPage />
      </MemoryRouter>,
    )

    await userEvent.type(screen.getByLabelText(/title/i), '   ')
    await userEvent.click(screen.getByRole('button', { name: /create ticket/i }))
    expect(await screen.findByText(/title is required/i)).toBeInTheDocument()
    expect(ticketsApi.createTicket).not.toHaveBeenCalled()
  })

  it('creates a ticket and navigates to details', async () => {
    vi.mocked(ticketsApi.createTicket).mockResolvedValue({
      id: 42,
      title: 'New ticket',
      description: null,
      priority: null,
      assignee: null,
      status: 'OPEN',
    })

    render(
      <MemoryRouter initialEntries={['/tickets/new']}>
        <Routes>
          <Route path="/tickets/new" element={<CreateTicketPage />} />
          <Route path="/tickets/:id" element={<div>Ticket detail 42</div>} />
        </Routes>
      </MemoryRouter>,
    )

    await userEvent.type(screen.getByLabelText(/title/i), 'New ticket')
    await userEvent.click(screen.getByRole('button', { name: /create ticket/i }))

    await waitFor(() => {
      expect(ticketsApi.createTicket).toHaveBeenCalled()
    })
    expect(await screen.findByText('Ticket detail 42')).toBeInTheDocument()
  })

  it('shows API validation errors', async () => {
    vi.mocked(ticketsApi.createTicket).mockRejectedValue(
      new ApiError(400, {
        message: 'Validation failed',
        errors: [{ field: 'title', message: 'must not be blank' }],
      }),
    )

    render(
      <MemoryRouter>
        <CreateTicketPage />
      </MemoryRouter>,
    )

    await userEvent.type(screen.getByLabelText(/title/i), 'Bad')
    await userEvent.click(screen.getByRole('button', { name: /create ticket/i }))

    expect(await screen.findByText(/validation failed/i)).toBeInTheDocument()
    expect(screen.getAllByText(/title: must not be blank/i).length).toBeGreaterThan(0)
  })
})
