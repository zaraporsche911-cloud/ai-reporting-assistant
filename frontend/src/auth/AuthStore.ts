import { createContext } from 'react'
import type { UserSummary } from '../types/api'

export interface AuthContextValue {
  user: UserSummary | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  register: (fullName: string, email: string, password: string) => Promise<void>
  logout: () => void
  refreshUser: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)
