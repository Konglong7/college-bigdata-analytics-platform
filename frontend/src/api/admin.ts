import request from '@/utils/request'
import { UniversityCard, UniversityQuery, PageResult } from '@/api/university'

export interface UniversityFormData {
  id?: number
  schoolName: string
  schoolCode?: string
  province: string
  city: string
  schoolType: string
  schoolLevel: string
  establishYear?: number
  introduction?: string
}

export const getAdminUniversityPage = (params: UniversityQuery) => {
  return request<any, PageResult<UniversityCard>>({
    url: '/admin/university/page',
    method: 'get',
    params
  })
}

export const addUniversity = (data: UniversityFormData) => {
  return request<any, void>({
    url: '/admin/university',
    method: 'post',
    data
  })
}

export const updateUniversity = (data: UniversityFormData) => {
  return request<any, void>({
    url: '/admin/university',
    method: 'put',
    data
  })
}

export const deleteUniversity = (id: number) => {
  return request<any, void>({
    url: `/admin/university/${id}`,
    method: 'delete'
  })
}
