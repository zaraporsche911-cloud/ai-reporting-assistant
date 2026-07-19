import axios, { AxiosError } from 'axios'
import type { ProblemDetails } from '../types/api'

const TOKEN_KEY = 'fleet-reporting.accessToken'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api/v1',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/')) {
      window.dispatchEvent(new Event('fleet-reporting:unauthorized'))
    }
    return Promise.reject(error)
  },
)

export function storeAccessToken(token: string) { sessionStorage.setItem(TOKEN_KEY, token) }
export function clearAccessToken() { sessionStorage.removeItem(TOKEN_KEY) }

export function getProblemMessage(error: unknown): string {
  if (axios.isAxiosError<ProblemDetails>(error)) {
    const problem = error.response?.data
    if (problem?.errors) {
      return Object.entries(problem.errors)
        .map(([field, message]) => `${field}: ${message}`)
        .join(' | ')
    }
    return problem?.detail ?? problem?.title ?? 'The request could not be completed.'
  }
  return error instanceof Error ? error.message : 'Something unexpected happened.'
}

export async function downloadFile(url: string) {
  const response = await api.get<Blob>(url, { responseType: 'blob' })
  const disposition = response.headers['content-disposition'] as string | undefined
  const filename = disposition?.match(/filename="([^"]+)"/)?.[1] ?? 'fleetops-report'
  const objectUrl = URL.createObjectURL(response.data)
  const anchor = document.createElement('a')
  anchor.href = objectUrl
  anchor.download = filename
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(objectUrl)
}
