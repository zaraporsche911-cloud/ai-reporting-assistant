import { CheckCircle2, X, XCircle } from 'lucide-react'
import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'

type Toast = { id: number; message: string; tone: 'success' | 'error' }
type ToastContextValue = { success: (message: string) => void; error: (message: string) => void }
const ToastContext = createContext<ToastContextValue | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])
  const push = useCallback((message: string, tone: Toast['tone']) => {
    const id = Date.now() + Math.random()
    setToasts((items) => [...items, { id, message, tone }])
    window.setTimeout(() => setToasts((items) => items.filter((item) => item.id !== id)), 4500)
  }, [])
  const value = useMemo(() => ({ success: (message: string) => push(message, 'success'), error: (message: string) => push(message, 'error') }), [push])
  return <ToastContext.Provider value={value}>{children}<div aria-live="polite" className="fixed bottom-5 right-5 z-50 grid w-[min(90vw,24rem)] gap-2">{toasts.map((toast) => <div key={toast.id} className={`flex items-center gap-3 rounded-xl border p-4 shadow-lg ${toast.tone === 'success' ? 'border-emerald-200 bg-white text-emerald-800' : 'border-red-200 bg-white text-red-800'}`}>{toast.tone === 'success' ? <CheckCircle2 size={19} /> : <XCircle size={19} />}<span className="flex-1 text-sm font-semibold">{toast.message}</span><button aria-label="Dismiss" onClick={() => setToasts((items) => items.filter((item) => item.id !== toast.id))}><X size={17} /></button></div>)}</div></ToastContext.Provider>
}

export function useToast() {
  const context = useContext(ToastContext)
  if (!context) throw new Error('useToast must be used inside ToastProvider')
  return context
}
