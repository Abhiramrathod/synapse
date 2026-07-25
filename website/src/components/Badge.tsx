import { ReactNode } from 'react'

interface BadgeProps {
  children: ReactNode
  variant?: 'default' | 'green' | 'blue' | 'purple'
}

export default function Badge({ children, variant = 'default' }: BadgeProps) {
  const styles = {
    default: 'bg-gray-800/50 text-gray-300 border-gray-700/50',
    green: 'bg-green-500/10 text-green-400 border-green-500/20',
    blue: 'bg-synapse-500/10 text-synapse-400 border-synapse-500/20',
    purple: 'bg-purple-500/10 text-purple-400 border-purple-500/20',
  }

  return (
    <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium border ${styles[variant]}`}>
      {children}
    </span>
  )
}
