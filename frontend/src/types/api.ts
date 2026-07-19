export type UserRole = 'ADMIN' | 'FLEET_MANAGER' | 'OPERATIONS_MANAGER' | 'ANALYST' | 'VIEWER'
export type ReportStatus = 'SUCCEEDED' | 'FAILED'
export type VisualizationType = 'KPI' | 'TABLE' | 'BAR_CHART' | 'LINE_CHART' | 'AREA_CHART' | 'DONUT_CHART'

export interface UserSummary {
  id: number
  fullName: string
  email: string
  role: UserRole
  enabled: boolean
}

export interface AuthResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresAt: string
  user: UserSummary
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface ProblemDetails {
  title?: string
  detail?: string
  status?: number
  errors?: Record<string, string>
  correlationId?: string
}

export interface Conversation {
  id: number
  title: string
  createdAt: string
  updatedAt: string
}

export interface Message {
  id: number
  author: 'USER' | 'ASSISTANT'
  content: string
  generatedReportId: number | null
  createdAt: string
}

export interface ConversationDetails {
  conversation: Conversation
  messages: Message[]
}

export interface DateRange { type: string; from: string | null; to: string | null }
export interface ReportIntent {
  reportType: string
  dateRange: DateRange
  comparisonDateRange: DateRange | null
  filters: { vehicleIds: number[]; driverIds: number[]; statuses: string[]; severityLevels: string[] }
  groupBy: string
  metrics: string[]
  sort: { field: string; direction: string } | null
  limit: number
  visualization: VisualizationType
}

export interface ReportResult {
  reportType: string
  title: string
  dataSource: string
  from: string
  to: string
  visualization: VisualizationType
  kpis: Array<{ label: string; value: string; unit: string | null; trend: string | null }>
  columns: Array<{ key: string; label: string; unit: string | null }>
  rows: Array<Record<string, unknown>>
  chart: { categoryKey: string; valueKeys: string[]; points: Array<Record<string, unknown>> } | null
  notices: string[]
}

export interface GeneratedReport {
  id: number
  conversationId: number | null
  question: string
  reportType: string
  intent: ReportIntent
  result: ReportResult | null
  summary: string | null
  status: ReportStatus
  executionTimeMs: number
  aiProvider: string | null
  aiModel: string | null
  errorDetail: string | null
  createdAt: string
}

export interface SavedReport {
  id: number
  title: string
  description: string | null
  tags: string | null
  favorite: boolean
  pinned: boolean
  sharedInternally: boolean
  report: GeneratedReport
  createdAt: string
  updatedAt: string
}

export interface AssistantResponse {
  conversation: Conversation
  message: Message
  report: GeneratedReport | null
  clarificationRequired: boolean
  suggestedQuestions: string[]
}

export interface CatalogueEntry {
  type: string
  displayName: string
  description: string
  requiredCapabilities: string[]
  maximumResultSize: number
  defaultVisualization: VisualizationType
  available: boolean
  availabilityMessage: string
}

export interface DashboardData {
  fleetMode: string
  fleet: { totalVehicles: number; activeVehicles: number; vehiclesInMaintenance: number; activeDrivers: number; openAnomalies: number; criticalOpenAnomalies: number }
  usage: { generatedReports: number; successfulReports: number; savedReports: number; conversations: number }
  recentReports: GeneratedReport[]
  pinnedReports: SavedReport[]
  recentConversations: Conversation[]
  reportUsageByType: Array<{ reportType: string; count: number }>
  quickQuestions: string[]
}

export interface User extends UserSummary { createdAt: string }
export interface ProviderStatus { id: string; active: boolean; available: boolean; message: string; model: string }
export interface PromptTemplate { id: number; key: string; displayName: string; content: string; enabled: boolean; updatedAt: string }
export interface AuditLog { id: number; actorEmail: string | null; action: string; resourceType: string; resourceId: string | null; detail: string | null; correlationId: string | null; createdAt: string }
export interface SystemStatus { databaseAvailable: boolean; fleetMode: string; fleetAvailable: boolean; fleetMessage: string; activeAiProvider: string; configuredModel: string; providers: ProviderStatus[]; securityNotice: string }
