import request, { rawClient } from './request'

export function getDashboardStats() {
  return request.get('/stats/dashboard')
}

export function getHealthStatus() {
  return rawClient.get('/actuator/health').then(response => response.data)
}