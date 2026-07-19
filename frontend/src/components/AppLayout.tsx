import { Activity, Bot, ChevronDown, ClipboardList, FileClock, FileText, LayoutDashboard, LogOut, Menu, Moon, PanelLeftClose, Settings, ShieldCheck, Sparkles, Sun, UserCircle, Users, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import clsx from 'clsx'
import { useAuth } from '../auth/useAuth'

const primary = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/assistant', label: 'AI Assistant', icon: Bot },
  { to: '/reports', label: 'Report catalogue', icon: FileText },
  { to: '/saved', label: 'Saved reports', icon: ClipboardList },
  { to: '/history', label: 'History', icon: FileClock },
]
const admin = [
  { to: '/admin/users', label: 'Users', icon: Users },
  { to: '/admin/audit', label: 'Audit log', icon: ShieldCheck },
  { to: '/admin/status', label: 'System status', icon: Activity },
]

export function AppLayout() {
  const { user, logout } = useAuth()
  const location = useLocation()
  const [mobileOpen, setMobileOpen] = useState(false)
  const [collapsed, setCollapsed] = useState(false)
  const [dark, setDark] = useState(() => localStorage.getItem('reporting-theme') === 'dark')
  const [accountOpen, setAccountOpen] = useState(false)
  useEffect(() => setMobileOpen(false), [location.pathname])
  useEffect(() => { document.documentElement.classList.toggle('dark', dark); localStorage.setItem('reporting-theme', dark ? 'dark' : 'light') }, [dark])

  const navigation = (mobile = false) => <div className="flex h-full flex-col">
    <div className="flex h-18 items-center gap-3 border-b border-white/10 px-5">
      <div className="grid size-10 shrink-0 place-items-center rounded-xl bg-white/12 text-white"><Sparkles size={21} /></div>
      {(!collapsed || mobile) && <div><p className="font-bold tracking-tight">FleetOps</p><p className="text-xs text-emerald-100/70">AI Reporting</p></div>}
      {mobile && <button className="ml-auto rounded-lg p-2 hover:bg-white/10" aria-label="Close navigation" onClick={() => setMobileOpen(false)}><X /></button>}
    </div>
    <nav aria-label="Primary navigation" className="flex-1 space-y-1 overflow-y-auto p-3">
      {primary.map((item) => <NavItem key={item.to} item={item} collapsed={collapsed && !mobile} />)}
      {user?.role === 'ADMIN' && <><p className={clsx('px-3 pb-1 pt-7 text-[10px] font-bold uppercase tracking-[.18em] text-emerald-100/50', collapsed && !mobile && 'sr-only')}>Administration</p>{admin.map((item) => <NavItem key={item.to} item={item} collapsed={collapsed && !mobile} />)}</>}
    </nav>
    <div className="border-t border-white/10 p-3">
      <NavItem item={{ to: '/settings', label: 'Settings', icon: Settings }} collapsed={collapsed && !mobile} />
    </div>
  </div>

  return <div className="min-h-screen bg-canvas dark:bg-slate-950">
    <aside className={clsx('fixed inset-y-0 left-0 z-40 hidden bg-brand-800 text-white transition-all lg:block', collapsed ? 'w-20' : 'w-64')}>{navigation()}<button onClick={() => setCollapsed((value) => !value)} aria-label="Toggle navigation width" className="absolute -right-3 top-22 grid size-7 place-items-center rounded-full border border-line bg-white text-ink-800 shadow dark:border-slate-700 dark:bg-slate-900 dark:text-white"><PanelLeftClose size={15} className={collapsed ? 'rotate-180' : ''} /></button></aside>
    {mobileOpen && <><button className="fixed inset-0 z-40 bg-slate-950/50 lg:hidden" aria-label="Close navigation overlay" onClick={() => setMobileOpen(false)} /><aside className="fixed inset-y-0 left-0 z-50 w-72 bg-brand-800 text-white lg:hidden">{navigation(true)}</aside></>}
    <div className={clsx('transition-all', collapsed ? 'lg:pl-20' : 'lg:pl-64')}>
      <header className="sticky top-0 z-30 flex h-18 items-center border-b border-line bg-white/90 px-4 backdrop-blur sm:px-6 dark:border-slate-800 dark:bg-slate-950/90">
        <button aria-label="Open navigation" className="focus-ring rounded-xl p-2 lg:hidden" onClick={() => setMobileOpen(true)}><Menu /></button>
        <div className="ml-auto flex items-center gap-2">
          <button className="focus-ring rounded-xl p-2.5 text-ink-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800" aria-label={dark ? 'Use light theme' : 'Use dark theme'} onClick={() => setDark((value) => !value)}>{dark ? <Sun size={20} /> : <Moon size={20} />}</button>
          <div className="relative">
            <button className="focus-ring flex items-center gap-3 rounded-xl px-2 py-1.5 hover:bg-slate-100 dark:hover:bg-slate-800" aria-expanded={accountOpen} onClick={() => setAccountOpen((value) => !value)}>
              <div className="grid size-9 place-items-center rounded-full bg-brand-100 font-bold text-brand-800">{user?.fullName.charAt(0).toUpperCase()}</div>
              <div className="hidden text-left sm:block"><p className="max-w-40 truncate text-sm font-semibold">{user?.fullName}</p><p className="text-xs text-ink-600 dark:text-slate-400">{user?.role.replaceAll('_', ' ')}</p></div><ChevronDown size={15} />
            </button>
            {accountOpen && <div className="absolute right-0 mt-2 w-52 rounded-xl border border-line bg-white p-2 shadow-xl dark:border-slate-700 dark:bg-slate-900"><NavLink to="/profile" className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm hover:bg-slate-100 dark:hover:bg-slate-800"><UserCircle size={17} />Profile</NavLink><button onClick={logout} className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-red-600 hover:bg-red-50 dark:hover:bg-red-950"><LogOut size={17} />Sign out</button></div>}
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-[1600px] p-4 sm:p-6 lg:p-8"><Outlet /></main>
    </div>
  </div>
}

function NavItem({ item, collapsed }: { item: { to: string; label: string; icon: typeof LayoutDashboard }; collapsed: boolean }) {
  const Icon = item.icon
  return <NavLink title={collapsed ? item.label : undefined} to={item.to} end={item.to === '/'} className={({ isActive }) => clsx('focus-ring flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold transition', isActive ? 'bg-white text-brand-800 shadow-sm' : 'text-emerald-50/80 hover:bg-white/10 hover:text-white', collapsed && 'justify-center px-2')}><Icon size={19} />{!collapsed && <span>{item.label}</span>}</NavLink>
}
