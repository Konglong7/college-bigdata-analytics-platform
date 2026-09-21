import request from '@/utils/request'

export interface UniversityQuery {
  page?: number
  size?: number
  schoolName?: string
  province?: string
  schoolLevel?: string
  schoolType?: string
}

export interface UniversityCard {
  id: number
  schoolName: string
  schoolCode?: string
  province: string
  city: string
  schoolType: string
  schoolLevel: string
  establishYear?: number
  department?: string
  introduction?: string
  schoolSite?: string
  gaokaoSite?: string
  belong?: string
  dualClassName?: string
  natureName?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface UnivDetail {
  id: number
  schoolName: string
  schoolCode: string
  province: string
  city: string
  schoolType: string
  schoolLevel: string
  establishYear: number
  department: string
  introduction: string
  tags: string[]
  // 官方网站与源站核验直达链接
  schoolSite?: string
  site?: string
  gaokaoSite?: string
  gaokaoScoreSite?: string
  chsiSite?: string
  // 官方联络与办学资质
  belong?: string
  natureName?: string
  dualClassName?: string
  phone?: string
  email?: string
  address?: string
  postcode?: string
  // 权威排名与科研指标
  ruankeRank?: number
  qsRank?: number
  xyhRank?: number
  numDoctor?: number
  numMaster?: number
  numAcademician?: number
  numLibrary?: string
  numLab?: number
  // 图表数据
  radarIndicators: Array<{ name: string; max: number }>
  radarValues: number[]
  rankYears: string[]
  rankValues: number[]
  rankCompare?: Array<{ name: string; value: number }>
  scoreYears: string[]
  scoreSeries: Array<{ name: string; data: number[] }>
  majorPie: Array<{ name: string; value: number }>
  maleRatio: number
  femaleRatio: number
}

export const pageUniversities = (params: UniversityQuery) => {
  return request<any, PageResult<UniversityCard>>({
    url: '/university/page',
    method: 'get',
    params
  })
}

export const getUniversityDetail = (id: number | string) => {
  return request<any, UnivDetail>({
    url: `/university/${id}/detail`,
    method: 'get'
  })
}
