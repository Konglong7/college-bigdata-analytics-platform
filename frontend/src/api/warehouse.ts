import request from '@/utils/request'

export interface WarehouseMetaData {
  metrics: {
    univCount: string
    majorCount: string
    enrollCount: string
    storageSize: string
  }
  layers: Array<{
    name: string
    class: string
    tables: string[]
  }>
  schemas: Array<{
    tableName: string
    engine: string
    fields: Array<{
      name: string
      type: string
    }>
  }>
}

export const getWarehouseMeta = () => {
  return request<any, WarehouseMetaData>({
    url: '/warehouse/meta',
    method: 'get'
  })
}
