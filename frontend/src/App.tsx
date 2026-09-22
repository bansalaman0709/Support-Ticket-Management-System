import { Link, Navigate, Route, Routes } from 'react-router-dom'
import { CreateTicketPage } from './pages/CreateTicketPage'
import { TicketDetailPage } from './pages/TicketDetailPage'
import { TicketListPage } from './pages/TicketListPage'

export default function App() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <Link to="/" className="brand">
          Support Tickets
        </Link>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<TicketListPage />} />
          <Route path="/tickets/new" element={<CreateTicketPage />} />
          <Route path="/tickets/:id" element={<TicketDetailPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  )
}
