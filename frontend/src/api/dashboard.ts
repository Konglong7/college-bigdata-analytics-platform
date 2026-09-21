import request from '@/utils/request'

export interface DashboardStats {
  totalUniversity: number
  undergraduateCount: number
  majorCount: number
  provinceCount: number
}

export interface MapDataItem {
  name: string
  value: number
}

export interface TypeRatioItem {
  name: string
  value: number
}

export interface GrowthTrendData {
  years: string[]
  values: number[]
  decadeLabels?: string[]
  decadeCounts?: number[]
  decadeAccum?: number[]
}

export interface ProvinceTop10Data {
  provinces: string[]
  values: number[]
}

export interface HotMajorItem {
  name: string
  value: number
}

export interface EnrollTrendData {
  years: string[]
  undergraduate: number[]
  juniorCollege: number[]
}

export const getStatistics = () => {
  return request<any, DashboardStats>({
    url: '/dashboard/statistics',
    method: 'get'
  })
}

export const getMapDistribution = () => {
  return request<any, MapDataItem[]>({
    url: '/dashboard/map-distribution',
    method: 'get'
  })
}

export const getTypeRatio = () => {
  return request<any, TypeRatioItem[]>({
    url: '/dashboard/type-ratio',
    method: 'get'
  })
}

export const getGrowthTrend = () => {
  return request<any, GrowthTrendData>({
    url: '/dashboard/growth-trend',
    method: 'get'
  })
}

export const getProvinceTop10 = () => {
  return request<any, ProvinceTop10Data>({
    url: '/dashboard/province-top10',
    method: 'get'
  })
}

export const getHotMajors = () => {
  return request<any, HotMajorItem[]>({
    url: '/dashboard/hot-majors',
    method: 'get'
  })
}

export const getEnrollTrend = () => {
  return request<any, EnrollTrendData>({
    url: '/dashboard/enroll-trend',
    method: 'get'
  })
}
