export const BORROW_STATUS_MAP = {
  0: { label: '申请中', className: 'pending' },
  1: { label: '待取书', className: 'primary' },
  2: { label: '借阅中', className: 'success' },
  3: { label: '已归还', className: 'muted' },
  4: { label: '已拒绝', className: 'danger' },
  5: { label: '逾期', className: 'danger' }
}

export const RESERVATION_STATUS_MAP = {
  0: { label: '预约中', className: 'pending' },
  1: { label: '已通知', className: 'success' },
  2: { label: '已取消', className: 'muted' },
  3: { label: '已过期', className: 'danger' }
}

export const NOTICE_TYPE_MAP = {
  1: { label: '信息', className: 'info' },
  2: { label: '成功', className: 'success' },
  3: { label: '警告', className: 'warning' }
}

export const READER_TYPE_MAP = {
  general: '普通读者',
  student: '学生读者',
  teacher: '教师读者'
}

export function getBorrowStatus(status) {
  return BORROW_STATUS_MAP[status] || { label: '未知', className: 'muted' }
}

export function getReservationStatus(status) {
  return RESERVATION_STATUS_MAP[status] || { label: '未知', className: 'muted' }
}

export function getNoticeType(type) {
  return NOTICE_TYPE_MAP[type] || { label: '通知', className: 'info' }
}

export function getReaderTypeLabel(type) {
  return READER_TYPE_MAP[type] || type || '-'
}