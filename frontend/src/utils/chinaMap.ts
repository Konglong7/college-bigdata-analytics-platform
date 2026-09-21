import * as echarts from 'echarts'

let mapRegistered = false

export async function ensureChinaMap() {
  if (mapRegistered) return
  try {
    const res = await fetch('/china.json')
    const geoJson = await res.json()
    echarts.registerMap('china', geoJson)
    mapRegistered = true
  } catch (e) {
    console.error('加载全国地图 GeoJSON 失败:', e)
  }
}
