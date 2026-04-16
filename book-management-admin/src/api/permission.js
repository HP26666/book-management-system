import request from '@/utils/request'

export const getPermissionTree = () => request.get('/permissions/tree')
