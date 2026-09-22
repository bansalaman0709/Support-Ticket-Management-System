import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { TicketListPage } from '../pages/TicketListPage'
import * as ticketsApi from '../api/tickets'
import { ApiError } from '../types'

vi.mock('../api/tickets')

describe('TicketListPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('lists tickets and supports empty state', async () => {
    vi.mocked(ticketsApi.listTickets).mockResolvedValueOnce([
      {
        id: 1,
        title: 'Login failure',
        description: 'SSO',
        priority: 'HIGH',
        assignee: 'alice',
        status: 'OPEN',
      },
    ])

    render(
      <MemoryRouter>
        <TicketListPage />
      </MemoryRouter>,
    )

    expect(screen.getByRole('status')).toHaveTextContent(/loading/i)
    expect(await screen.findByText('Login failure')).toBeInTheDocument()
    expect(document.querySelector('.status-OPEN')).toHaveTextContent('OPEN')

    vi.mocked(ticketsApi.listTickets).mockResolvedValueOnce([])
    await userEvent.selectOptions(screen.getByLabelText(/filter by status/i), 'CLOSED')

    await waitFor(() => {
      expect(screen.getByText(/no tickets found/i)).toBeInTheDocument()
    })
  })

  it('shows meaningful API errors', async () => {
    vi.mocked(ticketsApi.listTickets).mockRejectedValueOnce(
      new ApiError(400, {
        message: "Query parameter 'q' must not be blank",
        errors: [],
      }),
    )

    render(
      <MemoryRouter>
        <TicketListPage />
      </MemoryRouter>,
    )

    expect(
      await screen.findByText(/Query parameter 'q' must not be blank/i),
    ).toBeInTheDocument()
  })

  it('searches by keyword without sending blank q', async () => {
    vi.mocked(ticketsApi.listTickets).mockResolvedValue([])

    render(
      <MemoryRouter>
        <TicketListPage />
      </MemoryRouter>,
    )

    await screen.findByText(/no tickets found/i)
    await userEvent.type(screen.getByLabelText(/search by keyword/i), 'login')
    await userEvent.click(screen.getByRole('button', { name: /^search$/i }))

    await waitFor(() => {
      expect(ticketsApi.listTickets).toHaveBeenLastCalledWith({
        q: 'login',
        status: '',
      })
    })
  })

  it('filters by each ticket status value', async () => {
    vi.mocked(ticketsApi.listTickets).mockResolvedValue([])

    render(
      <MemoryRouter>
        <TicketListPage />
      </MemoryRouter>,
    )

    await screen.findByText(/no tickets found/i)

    for (const status of ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED'] as const) {
      await userEvent.selectOptions(screen.getByLabelText(/filter by status/i), status)
      await waitFor(() => {
        expect(ticketsApi.listTickets).toHaveBeenLastCalledWith({
          q: undefined,
          status,
        })
      })
    }
  })
})
