import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { authApi } from '../api'
import { clearAccessToken, storeAccessToken } from '../api/client'
import type { AuthResponse, UserSummary } from '../types/api'
import { AuthContext, type AuthContextValue } from './AuthStore'

const USER_KEY = 'fleet-reporting.user'

function readUser(): UserSummary | null {
  const stored = sessionStorage.getItem(USER_KEY)
  if (!stored) return null
  try { return JSON.parse(stored) as UserSummary } catch { sessionStorage.removeItem(USER_KEY); return null }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(readUser)

  const accept = useCallback((response: AuthResponse) => {
    storeAccessToken(response.accessToken)
    sessionStorage.setItem(USER_KEY, JSON.stringify(response.user))
    setUser(response.user)
  }, [])

  const logout = useCallback(() => {
    clearAccessToken()
    sessionStorage.removeItem(USER_KEY)
    setUser(null)
  }, [])

  const refreshUser = useCallback(async () => {
    const latest = await authApi.me()
    sessionStorage.setItem(USER_KEY, JSON.stringify(latest))
    setUser(latest)
  }, [])

  useEffect(() => {
    window.addEventListener('fleet-reporting:unauthorized', logout)
    return () => window.removeEventListener('fleet-reporting:unauthorized', logout)
  }, [logout])

  const value = useMemo<AuthContextValue>(() => ({
    user,
    isAuthenticated: user !== null,
    login: async (email, password) => accept(await authApi.login(email, password)),
    register: async (fullName, email, password) => accept(await authApi.register(fullName, email, password)),
    logout,
    refreshUser,
  }), [accept, logout, refreshUser, user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
