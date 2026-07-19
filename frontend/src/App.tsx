import { lazy, Suspense } from 'react'
import { Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/AppLayout'
import { ProtectedRoute, RoleGuard } from './components/Guards'
import { LoadingState } from './components/ui'

const AdminUsersPage = lazy(() => import('./pages/AdminUsersPage').then((module) => ({ default: module.AdminUsersPage })))
const AssistantPage = lazy(() => import('./pages/AssistantPage').then((module) => ({ default: module.AssistantPage })))
const AuditLogPage = lazy(() => import('./pages/AuditLogPage').then((module) => ({ default: module.AuditLogPage })))
const DashboardPage = lazy(() => import('./pages/DashboardPage').then((module) => ({ default: module.DashboardPage })))
const HistoryPage = lazy(() => import('./pages/HistoryPage').then((module) => ({ default: module.HistoryPage })))
const LoginPage = lazy(() => import('./pages/LoginPage').then((module) => ({ default: module.LoginPage })))
const ProfilePage = lazy(() => import('./pages/ProfilePage').then((module) => ({ default: module.ProfilePage })))
const ReportDetailsPage = lazy(() => import('./pages/ReportDetailsPage').then((module) => ({ default: module.ReportDetailsPage })))
const ReportsPage = lazy(() => import('./pages/ReportsPage').then((module) => ({ default: module.ReportsPage })))
const SavedReportsPage = lazy(() => import('./pages/SavedReportsPage').then((module) => ({ default: module.SavedReportsPage })))
const SettingsPage = lazy(() => import('./pages/SettingsPage').then((module) => ({ default: module.SettingsPage })))
const SystemStatusPage = lazy(() => import('./pages/SystemStatusPage').then((module) => ({ default: module.SystemStatusPage })))

export default function App() {
  return <Suspense fallback={<LoadingState label="Loading workspace" />}><Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route element={<ProtectedRoute />}><Route element={<AppLayout />}>
      <Route index element={<DashboardPage />} />
      <Route path="assistant" element={<AssistantPage />} />
      <Route path="reports" element={<ReportsPage />} />
      <Route path="reports/:id" element={<ReportDetailsPage />} />
      <Route path="saved" element={<SavedReportsPage />} />
      <Route path="history" element={<HistoryPage />} />
      <Route path="profile" element={<ProfilePage />} />
      <Route path="settings" element={<SettingsPage />} />
      <Route element={<RoleGuard roles={['ADMIN']} />}>
        <Route path="admin/users" element={<AdminUsersPage />} />
        <Route path="admin/audit" element={<AuditLogPage />} />
        <Route path="admin/status" element={<SystemStatusPage />} />
      </Route>
    </Route></Route>
    <Route path="*" element={<main className="grid min-h-screen place-items-center p-6 text-center"><div><p className="text-7xl font-bold text-brand-700">404</p><h1 className="mt-4 text-2xl font-bold">Page not found</h1><a className="mt-5 inline-block font-semibold text-brand-700" href="/">Return home</a></div></main>} />
  </Routes></Suspense>
}
