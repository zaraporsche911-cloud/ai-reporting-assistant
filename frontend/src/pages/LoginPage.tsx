import { zodResolver } from '@hookform/resolvers/zod'
import { BarChart3, CheckCircle2, ShieldCheck, Sparkles } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Navigate } from 'react-router-dom'
import { z } from 'zod'
import { useAuth } from '../auth/useAuth'
import { Button, Field, inputClass } from '../components/ui'
import { getProblemMessage } from '../api/client'

const schema = z.object({ fullName: z.string().min(2).optional(), email: z.email(), password: z.string().min(8) })
type FormValues = z.infer<typeof schema>

export function LoginPage() {
  const { isAuthenticated, login, register } = useAuth()
  const [registerMode, setRegisterMode] = useState(false)
  const [serverError, setServerError] = useState('')
  const { register: field, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({ resolver: zodResolver(schema) })
  if (isAuthenticated) return <Navigate to="/" replace />
  const submit = async (values: FormValues) => { setServerError(''); try { if (registerMode) await register(values.fullName ?? '', values.email, values.password); else await login(values.email, values.password) } catch (error) { setServerError(getProblemMessage(error)) } }
  return <main className="grid min-h-screen lg:grid-cols-[1.1fr_.9fr]">
    <section className="hidden bg-brand-800 p-14 text-white lg:flex lg:flex-col lg:justify-between">
      <div className="flex items-center gap-3"><div className="grid size-11 place-items-center rounded-xl bg-white/10"><Sparkles /></div><div><p className="text-lg font-bold">FleetOps</p><p className="text-xs text-emerald-100/70">AI Reporting Assistant</p></div></div>
      <div className="max-w-xl"><p className="text-sm font-bold uppercase tracking-[.2em] text-emerald-200">Operational intelligence</p><h1 className="mt-5 text-5xl font-bold leading-tight tracking-tight">Ask business questions. Get defensible fleet answers.</h1><p className="mt-6 text-lg leading-8 text-emerald-50/75">Natural-language intent is validated, executed through deterministic reporting logic, and explained with a transparent AI boundary.</p><div className="mt-10 grid gap-4 text-sm text-emerald-50/85"><Feature icon={<BarChart3 />} text="18 operational report types" /><Feature icon={<ShieldCheck />} text="Role-based access and full audit trail" /><Feature icon={<CheckCircle2 />} text="Exports that preserve data lineage" /></div></div>
      <p className="text-xs text-emerald-100/50">A portfolio-grade companion to Fleet Control Tower.</p>
    </section>
    <section className="flex items-center justify-center bg-white p-6 dark:bg-slate-950"><div className="w-full max-w-md"><div className="mb-10 lg:hidden"><p className="text-xl font-bold text-brand-800 dark:text-emerald-400">FleetOps AI Reporting</p></div><p className="text-xs font-bold uppercase tracking-[.18em] text-brand-700 dark:text-emerald-400">Secure workspace</p><h2 className="mt-3 text-3xl font-bold tracking-tight">{registerMode ? 'Create the first admin' : 'Welcome back'}</h2><p className="mt-2 text-sm text-ink-600 dark:text-slate-400">{registerMode ? 'Registration is only available while no users exist.' : 'Sign in to your reporting workspace.'}</p>
      <form className="mt-8 space-y-5" onSubmit={handleSubmit(submit)}>{registerMode && <Field label="Full name" error={errors.fullName?.message}><input className={inputClass} autoComplete="name" {...field('fullName')} /></Field>}<Field label="Email" error={errors.email?.message}><input className={inputClass} type="email" autoComplete="email" {...field('email')} /></Field><Field label="Password" error={errors.password?.message}><input className={inputClass} type="password" autoComplete={registerMode ? 'new-password' : 'current-password'} {...field('password')} /></Field>{serverError && <p role="alert" className="rounded-xl bg-red-50 p-3 text-sm text-red-700 dark:bg-red-950 dark:text-red-200">{serverError}</p>}<Button className="w-full" disabled={isSubmitting}>{isSubmitting ? 'Please wait…' : registerMode ? 'Create admin account' : 'Sign in'}</Button></form>
      <button className="focus-ring mt-6 text-sm font-semibold text-brand-700 dark:text-emerald-400" onClick={() => { setServerError(''); setRegisterMode((value) => !value) }}>{registerMode ? 'Already configured? Sign in' : 'First-time setup'}</button></div></section>
  </main>
}

function Feature({ icon, text }: { icon: React.ReactNode; text: string }) { return <div className="flex items-center gap-3">{icon}<span>{text}</span></div> }
