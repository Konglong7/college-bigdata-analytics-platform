import request from '@/utils/request'

export interface NodeItem {
  name: string
  status: string
  type: string
  updateTime: string
  throughput: string
}

export interface LogItem {
  time: string
  msg: string
  badge: string
  level: string
}

export interface TrafficTrendData {
  times: string[]
  series: Array<{
    name: string
    data: number[]
  }>
}

export const getNodes = () => {
  return request<any, NodeItem[]>({
    url: '/collect/nodes',
    method: 'get'
  })
}

export const getLogs = () => {
  return request<any, LogItem[]>({
    url: '/collect/logs',
    method: 'get'
  })
}

export const getTrafficTrend = () => {
  return request<any, TrafficTrendData>({
    url: '/collect/traffic-trend',
    method: 'get'
  })
}
