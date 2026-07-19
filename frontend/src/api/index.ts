import { api } from './client'
import type {
  AssistantResponse, AuditLog, AuthResponse, CatalogueEntry, Conversation, ConversationDetails,
  DashboardData, GeneratedReport, PageResponse, PromptTemplate, ProviderStatus, SavedReport,
  SystemStatus, User, UserRole, UserSummary,
} from '../types/api'

export const authApi = {
  login: (email: string, password: string) => api.post<AuthResponse>('/auth/login', { email, password }).then((r) => r.data),
  register: (fullName: string, email: string, password: string) => api.post<AuthResponse>('/auth/register', { fullName, email, password }).then((r) => r.data),
  me: () => api.get<UserSummary>('/auth/me').then((r) => r.data),
}

export const dashboardApi = { get: () => api.get<DashboardData>('/dashboard').then((r) => r.data) }

export const assistantApi = {
  query: (question: string, conversationId?: number) => api.post<AssistantResponse>('/assistant/query', { question, conversationId }).then((r) => r.data),
}

export const conversationApi = {
  list: () => api.get<PageResponse<Conversation>>('/conversations?size=50').then((r) => r.data),
  get: (id: number) => api.get<ConversationDetails>(`/conversations/${id}`).then((r) => r.data),
  rename: (id: number, title: string) => api.put<Conversation>(`/conversations/${id}`, { title }).then((r) => r.data),
  remove: (id: number) => api.delete(`/conversations/${id}`),
}

export const reportApi = {
  catalogue: () => api.get<CatalogueEntry[]>('/reports/catalogue').then((r) => r.data),
  list: (params = '') => api.get<PageResponse<GeneratedReport>>(`/reports${params ? `?${params}` : ''}`).then((r) => r.data),
  get: (id: number) => api.get<GeneratedReport>(`/reports/${id}`).then((r) => r.data),
  rerun: (id: number) => api.post<GeneratedReport>(`/reports/${id}/rerun`).then((r) => r.data),
  regenerate: (id: number) => api.post<GeneratedReport>(`/reports/${id}/regenerate-summary`).then((r) => r.data),
  save: (id: number, title: string) => api.post<SavedReport>(`/reports/${id}/save`, { title }).then((r) => r.data),
}

export const savedReportApi = {
  list: () => api.get<PageResponse<SavedReport>>('/saved-reports?size=50').then((r) => r.data),
  update: (report: SavedReport) => api.put<SavedReport>(`/saved-reports/${report.id}`, {
    title: report.title, description: report.description, tags: report.tags,
    favorite: report.favorite, pinned: report.pinned, sharedInternally: report.sharedInternally,
  }).then((r) => r.data),
  duplicate: (id: number) => api.post<SavedReport>(`/saved-reports/${id}/duplicate`).then((r) => r.data),
  remove: (id: number) => api.delete(`/saved-reports/${id}`),
}

export const profileApi = {
  get: () => api.get<User>('/profile').then((r) => r.data),
  update: (fullName: string) => api.put<User>('/profile', { fullName }).then((r) => r.data),
  preferences: () => api.get<{ darkMode: boolean; timezone: string }>('/profile/preferences').then((r) => r.data),
  updatePreferences: (darkMode: boolean, timezone: string) => api.put('/profile/preferences', { darkMode, timezone }).then((r) => r.data),
}

export const adminApi = {
  users: () => api.get<User[]>('/users').then((r) => r.data),
  createUser: (payload: { fullName: string; email: string; password: string; role: UserRole }) => api.post<User>('/users', payload).then((r) => r.data),
  updateUser: (id: number, role: UserRole, enabled: boolean) => api.put<User>(`/users/${id}`, { role, enabled }).then((r) => r.data),
  audit: () => api.get<PageResponse<AuditLog>>('/admin/audit-logs?size=100').then((r) => r.data),
  providers: () => api.get<ProviderStatus[]>('/admin/ai-providers').then((r) => r.data),
  changeProvider: (providerId: string) => api.put<ProviderStatus>('/admin/ai-providers/active', { providerId }).then((r) => r.data),
  prompts: () => api.get<PromptTemplate[]>('/admin/prompt-templates').then((r) => r.data),
  updatePrompt: (prompt: PromptTemplate) => api.put<PromptTemplate>(`/admin/prompt-templates/${prompt.id}`, {
    displayName: prompt.displayName, content: prompt.content, enabled: prompt.enabled,
  }).then((r) => r.data),
  status: () => api.get<SystemStatus>('/admin/system-status').then((r) => r.data),
}
