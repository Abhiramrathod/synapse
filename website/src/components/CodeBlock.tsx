import { useState } from 'react'
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter'
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism'
import { Copy, Check } from 'lucide-react'

interface CodeBlockProps {
  code: string
  language?: string
  title?: string
  showLineNumbers?: boolean
}

const customStyle = {
  ...vscDarkPlus,
  'pre[class*="language-"]': {
    ...vscDarkPlus['pre[class*="language-"]'],
    background: 'transparent',
    margin: 0,
    padding: '1.25rem',
    fontSize: '0.875rem',
    lineHeight: '1.7',
  },
  'code[class*="language-"]': {
    ...vscDarkPlus['code[class*="language-"]'],
    background: 'transparent',
    fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
  },
}

export default function CodeBlock({ code, language = 'java', title, showLineNumbers = true }: CodeBlockProps) {
  const [copied, setCopied] = useState(false)

  const handleCopy = async () => {
    await navigator.clipboard.writeText(code)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="code-block group">
      {title && (
        <div className="flex items-center justify-between px-5 py-2.5 border-b border-gray-800/50 bg-gray-900/50">
          <div className="flex items-center gap-2">
            <div className="flex gap-1.5">
              <div className="w-3 h-3 rounded-full bg-red-500/60" />
              <div className="w-3 h-3 rounded-full bg-yellow-500/60" />
              <div className="w-3 h-3 rounded-full bg-green-500/60" />
            </div>
            <span className="text-xs text-gray-500 font-mono ml-2">{title}</span>
          </div>
          <button
            onClick={handleCopy}
            className="flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs text-gray-400 hover:text-white hover:bg-gray-800/50 transition-all opacity-0 group-hover:opacity-100"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-green-400" /> : <Copy className="w-3.5 h-3.5" />}
            {copied ? 'Copied' : 'Copy'}
          </button>
        </div>
      )}
      {!title && (
        <button
          onClick={handleCopy}
          className="absolute top-3 right-3 flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs text-gray-400 hover:text-white hover:bg-gray-800/50 transition-all opacity-0 group-hover:opacity-100 z-10"
        >
          {copied ? <Check className="w-3.5 h-3.5 text-green-400" /> : <Copy className="w-3.5 h-3.5" />}
        </button>
      )}
      <SyntaxHighlighter
        language={language}
        style={customStyle}
        showLineNumbers={showLineNumbers}
        lineNumberStyle={{ color: '#4a5568', fontSize: '0.75rem', minWidth: '2.5em' }}
        wrapLines
      >
        {code.trim()}
      </SyntaxHighlighter>
    </div>
  )
}
