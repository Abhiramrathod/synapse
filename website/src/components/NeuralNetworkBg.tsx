import { useEffect, useRef } from 'react'

interface Node {
  x: number
  y: number
  vx: number
  vy: number
  radius: number
  color: string
  pulsePhase: number
  pulseSpeed: number
}

interface Connection {
  from: number
  to: number
  strength: number
  signalProgress: number
  signalSpeed: number
  signalActive: boolean
}

const COLORS = {
  nodeBlue: '#5c7cfa',
  nodePurple: '#8b5cf6',
  nodeGreen: '#00ff88',
  nodeCyan: '#00d4ff',
  lineDefault: 'rgba(92, 124, 250, 0.12)',
  lineActive: 'rgba(0, 255, 136, 0.4)',
  signal: '#00ff88',
}

const NODE_COLORS = [COLORS.nodeBlue, COLORS.nodePurple, COLORS.nodeGreen, COLORS.nodeCyan]

function createNodes(width: number, height: number, count: number): Node[] {
  return Array.from({ length: count }, () => ({
    x: Math.random() * width,
    y: Math.random() * height,
    vx: (Math.random() - 0.5) * 0.3,
    vy: (Math.random() - 0.5) * 0.3,
    radius: Math.random() * 2 + 1.5,
    color: NODE_COLORS[Math.floor(Math.random() * NODE_COLORS.length)],
    pulsePhase: Math.random() * Math.PI * 2,
    pulseSpeed: 0.005 + Math.random() * 0.01,
  }))
}

function createConnections(nodes: Node[], maxDist: number): Connection[] {
  const connections: Connection[] = []
  for (let i = 0; i < nodes.length; i++) {
    for (let j = i + 1; j < nodes.length; j++) {
      const dx = nodes[i].x - nodes[j].x
      const dy = nodes[i].y - nodes[j].y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < maxDist) {
        connections.push({
          from: i,
          to: j,
          strength: 1 - dist / maxDist,
          signalProgress: Math.random(),
          signalSpeed: 0.003 + Math.random() * 0.008,
          signalActive: Math.random() > 0.6,
        })
      }
    }
  }
  return connections
}

export default function NeuralNetworkBg() {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const animRef = useRef<number>(0)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    const ctx = canvas.getContext('2d')
    if (!ctx) return

    let width = window.innerWidth
    let height = window.innerHeight
    canvas.width = width
    canvas.height = height

    const nodeCount = Math.min(Math.floor((width * height) / 18000), 80)
    const maxDist = 180
    let nodes = createNodes(width, height, nodeCount)
    let connections = createConnections(nodes, maxDist)
    let time = 0

    const resize = () => {
      width = window.innerWidth
      height = window.innerHeight
      canvas.width = width
      canvas.height = height
      const newCount = Math.min(Math.floor((width * height) / 18000), 80)
      nodes = createNodes(width, height, newCount)
      connections = createConnections(nodes, maxDist)
    }

    window.addEventListener('resize', resize)

    const animate = () => {
      time++
      ctx.clearRect(0, 0, width, height)

      // Update nodes
      for (const node of nodes) {
        node.x += node.vx
        node.y += node.vy
        node.pulsePhase += node.pulseSpeed

        if (node.x < -20) node.x = width + 20
        if (node.x > width + 20) node.x = -20
        if (node.y < -20) node.y = height + 20
        if (node.y > height + 20) node.y = -20
      }

      // Update signals
      for (const conn of connections) {
        if (conn.signalActive) {
          conn.signalProgress += conn.signalSpeed
          if (conn.signalProgress >= 1) {
            conn.signalProgress = 0
            conn.signalActive = Math.random() > 0.4
          }
        } else if (Math.random() < 0.001) {
          conn.signalActive = true
          conn.signalProgress = 0
        }
      }

      // Recompute connections periodically
      if (time % 120 === 0) {
        connections = createConnections(nodes, maxDist)
      }

      // Draw connections
      for (const conn of connections) {
        const fromNode = nodes[conn.from]
        const toNode = nodes[conn.to]
        const dx = toNode.x - fromNode.x
        const dy = toNode.y - fromNode.y
        const dist = Math.sqrt(dx * dx + dy * dy)
        const alpha = (1 - dist / maxDist) * 0.15

        ctx.beginPath()
        ctx.moveTo(fromNode.x, fromNode.y)
        ctx.lineTo(toNode.x, toNode.y)
        ctx.strokeStyle = conn.signalActive
          ? `rgba(0, 255, 136, ${alpha * 2})`
          : `rgba(92, 124, 250, ${alpha})`
        ctx.lineWidth = conn.signalActive ? 1.2 : 0.6
        ctx.stroke()

        // Draw signal pulse
        if (conn.signalActive) {
          const sx = fromNode.x + dx * conn.signalProgress
          const sy = fromNode.y + dy * conn.signalProgress
          const pulseRadius = 2 + Math.sin(time * 0.1) * 1

          const gradient = ctx.createRadialGradient(sx, sy, 0, sx, sy, pulseRadius * 3)
          gradient.addColorStop(0, 'rgba(0, 255, 136, 0.8)')
          gradient.addColorStop(0.5, 'rgba(0, 212, 255, 0.3)')
          gradient.addColorStop(1, 'rgba(0, 255, 136, 0)')

          ctx.beginPath()
          ctx.arc(sx, sy, pulseRadius * 3, 0, Math.PI * 2)
          ctx.fillStyle = gradient
          ctx.fill()

          ctx.beginPath()
          ctx.arc(sx, sy, pulseRadius, 0, Math.PI * 2)
          ctx.fillStyle = COLORS.signal
          ctx.fill()
        }
      }

      // Draw nodes
      for (const node of nodes) {
        const pulse = Math.sin(node.pulsePhase) * 0.5 + 0.5
        const r = node.radius + pulse * 1.5

        // Glow
        const glow = ctx.createRadialGradient(node.x, node.y, 0, node.x, node.y, r * 6)
        glow.addColorStop(0, node.color + '30')
        glow.addColorStop(1, node.color + '00')
        ctx.beginPath()
        ctx.arc(node.x, node.y, r * 6, 0, Math.PI * 2)
        ctx.fillStyle = glow
        ctx.fill()

        // Core
        ctx.beginPath()
        ctx.arc(node.x, node.y, r, 0, Math.PI * 2)
        ctx.fillStyle = node.color
        ctx.globalAlpha = 0.6 + pulse * 0.4
        ctx.fill()
        ctx.globalAlpha = 1
      }

      animRef.current = requestAnimationFrame(animate)
    }

    animRef.current = requestAnimationFrame(animate)

    return () => {
      window.removeEventListener('resize', resize)
      cancelAnimationFrame(animRef.current)
    }
  }, [])

  return (
    <canvas
      ref={canvasRef}
      className="fixed inset-0 pointer-events-none"
      style={{ zIndex: 0, opacity: 0.7 }}
    />
  )
}
