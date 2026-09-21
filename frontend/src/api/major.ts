import request from '@/utils/request'

export interface EmploymentTop10Data {
  majors: string[]
  rates: number[]
}

export interface NewEmergingTrendData {
  years: string[]
  series: Array<{
    name: string
    data: number[]
  }>
}

export const getHeatCalendar = () => {
  return request<any, Array<[string, number]>>({
    url: '/major/heat-calendar',
    method: 'get'
  })
}

export const getEmploymentTop10 = () => {
  return request<any, EmploymentTop10Data>({
    url: '/major/employment-top10',
    method: 'get'
  })
}

export const getNewEmergingTrend = () => {
  return request<any, NewEmergingTrendData>({
    url: '/major/new-emerging-trend',
    method: 'get'
  })
}
